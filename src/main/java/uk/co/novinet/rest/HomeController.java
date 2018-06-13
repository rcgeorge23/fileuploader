package uk.co.novinet.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import uk.co.novinet.service.MailSenderService;
import uk.co.novinet.service.Member;
import uk.co.novinet.service.MemberService;
import uk.co.novinet.service.SftpService;

@Controller
public class HomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private SftpService sftpService;

    @GetMapping("/")
    public String get(@RequestParam(value = "token", required = false) String token) {
        Member member = memberService.findMemberByToken(token);

        if (member == null) {
            return "notFound";
        }

        if (member.hasCompletedMembershipForm()) {
            return "thankYou";
        }

        return "home";
    }

    @CrossOrigin
    @PostMapping(path = "/submit")
    public String submit(
            @RequestParam("identification") MultipartFile identification,
            @RequestParam("proofOfSchemeInvolvement") MultipartFile proofOfSchemeInvolvement,
            Member member) {
        try {
            memberService.update(member);
            Long currentTimeMillis = System.currentTimeMillis();
            String sanitisedEmailAddress = sanitise(member.getEmailAddress());
            String destinationPath = "/" + sanitisedEmailAddress + "/" + currentTimeMillis;

            sftpService.sendToSftpEndpoint(identification.getInputStream(), destinationPath, identification.getOriginalFilename());
            sftpService.sendToSftpEndpoint(proofOfSchemeInvolvement.getInputStream(), destinationPath, proofOfSchemeInvolvement.getOriginalFilename());

            mailSenderService.sendFollowUpEmail(member);
            return "thankYou";
        } catch (Exception e) {
            LOGGER.error("Unable to receive files and update member {}", member, e);
            return "error";
        }
    }

    private String sanitise(String toSanitise) {
        return toSanitise.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

}