package io.github.seaniestack.supportservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support-service/health")
@RequiredArgsConstructor
public class HealthTestController {

    @GetMapping
    public ResponseEntity<String> HealthCheck() {
        return new ResponseEntity<>("Support Service OK", HttpStatus.OK);
    }
}
