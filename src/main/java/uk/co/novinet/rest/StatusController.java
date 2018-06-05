package uk.co.novinet.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @CrossOrigin
    @GetMapping(path = "/status")
    public ResponseEntity getStatus() {
        return new ResponseEntity(HttpStatus.OK);
    }
}