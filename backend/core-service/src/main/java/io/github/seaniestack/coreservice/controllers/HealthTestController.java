package io.github.seaniestack.coreservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core-service/health")
@RequiredArgsConstructor
public class HealthTestController {

    @GetMapping
    public ResponseEntity<String> HealthCheck() {
        return new ResponseEntity<>("Core Service OK", HttpStatus.OK);
    }
}