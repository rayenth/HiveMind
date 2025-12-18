package com.security.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to the Security System Backend API");
        response.put("status", "Running");
        response.put("health_check", "/api/health");
        response.put("documentation", "/swagger-ui.html (if enabled)");
        return ResponseEntity.ok(response);
    }
}
