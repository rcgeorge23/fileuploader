package uk.co.novinet.rest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.co.novinet.service.Member;
import uk.co.novinet.service.MemberService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

@Controller
public class HomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @Value("${document.upload.destination.directory}")
    private String documentUploadDestinationDirectory;

    @Autowired
    private MemberService memberService;

    @GetMapping("/")
    public ModelAndView get(@RequestParam(value = "token", required = false) String token) {
        Member member = memberService.findMemberByToken(token);

        if (member == null) {
            return new ModelAndView("notFound");
        }

        return new ModelAndView("home", new HashMap<String, Member>() {{ put("member", member); }});
    }

    @CrossOrigin
    @PostMapping(path = "/submit")
    public ResponseEntity submit(@RequestParam("file") MultipartFile file, Member member) {
        try {
            memberService.update(member);
            IOUtils.write(file.getBytes(), new FileOutputStream(new File("/tmp/newFile.txt")));
            return new ResponseEntity( HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}