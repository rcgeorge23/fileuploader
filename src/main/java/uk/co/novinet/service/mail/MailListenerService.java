package uk.co.novinet.service.mail;

import com.sun.mail.imap.IMAPFolder;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.novinet.service.member.Member;
import uk.co.novinet.service.member.MemberService;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Service
public class MailListenerService {
    private static final List<Boolean> SEEN_FLAG_STATES = Arrays.asList(TRUE, FALSE);

    private static final Logger LOGGER = LoggerFactory.getLogger(MailListenerService.class);

    private static final Pattern ENQUIRY_PATTERN = Pattern.compile("Message Details: Email (?<emailAddress>.*) Name (?<name>.*) Subject");

    @Value("${imapHost}")
    private String imapHost;

    @Value("${imapPort}")
    private int imapPort;

    @Value("${imapUsername}")
    private String imapUsername;

    @Value("${imapPassword}")
    private String imapPassword;

    @Value("${imapProtocol}")
    private String imapProtocol;

    @Value("${processedFolderName}")
    private String processedFolderName;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private MemberService memberService;

    @Scheduled(initialDelayString = "${retrieveMailInitialDelayMilliseconds}", fixedRateString = "${retrieveMailIntervalMilliseconds}")
    public void retrieveMail() {
        LOGGER.info("Checking for new mail");

        IMAPFolder inbox = null;
        Store store = null;
        Message message = null;

        try {
            Session session = Session.getDefaultInstance(new Properties());
            store = session.getStore(imapProtocol);
            store.connect(imapHost, imapPort, imapUsername, imapPassword);
            inbox = (IMAPFolder) store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);

            for (Boolean originalSeenFlagState : SEEN_FLAG_STATES) {
                LOGGER.info("Going to check messages with SEEN FLAG={}", originalSeenFlagState);

                Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), originalSeenFlagState));

                LOGGER.info("Found {} messages to process", messages.length);

                for (Message m : messages) {
                    message = m;

                    String emailBody = getTextFromMessage(message);
                    Enquiry enquiry = extractEnquiry(emailBody);

                    if (enquiry == null) {
                        LOGGER.info("Enquiry not found in email body", emailBody);
                        LOGGER.info("Skipping...");
                        restoreOriginalMessageFlags(message, originalSeenFlagState);
                        continue;
                    }

                    LOGGER.info("Found enquiry: {}", enquiry);

                    Member member = memberService.createForumUserIfNecessary(enquiry);

                    if (member == null) {
                        LOGGER.info("We didn't create a forum user, so set this message to SEEN={} again", originalSeenFlagState);
                        restoreOriginalMessageFlags(message, originalSeenFlagState);
                    } else {
                        Date receivedDate = null;

                        if (message != null) {
                            receivedDate = message.getReceivedDate();
                        }

                        mailSenderService.sendFollowUpEmail(member);
                        moveEmailToProcessedFolder(message, store, inbox);

                        LOGGER.info(">>> " + enquiry.getName() + "\t " + member.getUsername() + "\t " + enquiry.getEmailAddress() + "\t " + receivedDate);
                    }
                }
            }

            LOGGER.info("Finished checking mail. Going back to sleep now.");
        } catch (Exception e) {
            LOGGER.error("An error occurred while trying to read emails", e);
        } finally {
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (MessagingException e) {
                    LOGGER.error("Error occurred trying to close inbox", e);
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    LOGGER.error("Error occurred trying to close store", e);
                }
            }
        }
    }

    private void moveEmailToProcessedFolder(Message message, Store store, IMAPFolder inbox) {
        LOGGER.info("Going to move message to processed folder");
        Folder processedFolder = null;
        try {
            processedFolder = store.getFolder(processedFolderName);
            if (processedFolder.exists()) {
                LOGGER.info("Processed folder {} exists", processedFolderName);
                processedFolder.open(Folder.READ_WRITE);

                LOGGER.info("Inbox contains {} messages", inbox.getMessageCount());
                LOGGER.info("{} contains {} messages", processedFolderName, processedFolder.getMessageCount());

                LOGGER.info("Copying message to {} folder", processedFolderName);
                inbox.copyMessages(new Message[]{message}, processedFolder);

                LOGGER.info("Deleting old message from Inbox");
                message.setFlag(Flags.Flag.DELETED, true);
                inbox.expunge();
            } else {
                LOGGER.info("Could not find processed folder '{}' so going to skip this and leave the message where it was.", processedFolderName);
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (processedFolder != null && processedFolder.exists() && processedFolder.isOpen()) {
                    processedFolder.close(true);
                }
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void restoreOriginalMessageFlags(Message message, boolean originalSeenFlagState) {
        try {
            LOGGER.info("Going to set current email status back to SEEN={}", originalSeenFlagState);
            if (message != null) {
                message.setFlag(Flags.Flag.SEEN, originalSeenFlagState);
            }
        } catch (MessagingException me) {
            LOGGER.error("An error occurred while trying to set email read status to unread", me);
        }
    }

    private Enquiry extractEnquiry(String emailBody) {
        Matcher matcher = ENQUIRY_PATTERN.matcher(emailBody);

        if (matcher.find()) {
            return new Enquiry(matcher.group("emailAddress"), matcher.group("name"));
        }

        return null;
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart)  throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }
}
