package uk.co.novinet.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.co.novinet.service.*;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    public String get(@RequestParam(value = "n", required = false) String newGuest, @RequestParam(value = "token", required = false) String token) {
        if ("t".equals(newGuest)) {
            return "home";
        }

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
    public ModelAndView submit(
            @RequestParam("identification") MultipartFile identification,
            @RequestParam("proofOfSchemeInvolvement") MultipartFile proofOfSchemeInvolvement,
            Member member,
            ModelMap model) {
        try {
            Long currentTimeMillis = System.currentTimeMillis();
            String sanitisedEmailAddress = sanitise(member.getEmailAddress());
            String destinationPath = sanitisedEmailAddress + "/" + currentTimeMillis;

            if (isNotBlank(identification.getOriginalFilename())) {
                sftpService.sendToSftpEndpoint(identification.getInputStream(), destinationPath, "identification-" + identification.getOriginalFilename());
            }

            if (isNotBlank(proofOfSchemeInvolvement.getOriginalFilename())) {
                sftpService.sendToSftpEndpoint(proofOfSchemeInvolvement.getInputStream(), destinationPath, "scheme-" + proofOfSchemeInvolvement.getOriginalFilename());
            }

            List<SftpDocument> uploadedDocuments = sftpService.getAllDocumentsForPath(destinationPath);

            if (!member.getMemberOfBigGroup() &&
                    (
                            isBlank(identification.getOriginalFilename()) ||
                            isBlank(proofOfSchemeInvolvement.getOriginalFilename()) ||
                                    uploadedDocuments.size() != 2 ||
                                    uploadedDocuments.get(0).getSize() <= 4096L ||
                                    uploadedDocuments.get(1).getSize() <= 4096L
                    )) {

                model.addAttribute("token", member.getToken());
                member.setCompletedMembershipForm(false);
                member.setDocumentUploadError(true);
            } else {
                member.setDocumentUploadError(false);
                member.setCompletedMembershipForm(true);
            }

            memberService.updateOrCreate(member);

            if (member.hasCompletedMembershipForm()) {
                mailSenderService.sendFollowUpEmail(member);
                return new ModelAndView("thankYou", model);
            }

            return new ModelAndView("redirect:/", model);
        } catch (Exception e) {
            LOGGER.error("Unable to receive files and update member {}", member, e);
            return new ModelAndView("error", model);
        }
    }

    private String sanitise(String toSanitise) {
        return toSanitise.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

}