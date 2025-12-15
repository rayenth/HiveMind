package com.security.backend.controller;

import com.security.backend.model.SecurityEvent;
import com.security.backend.service.SecurityEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final SecurityEventService service;

    @PostMapping
    public ResponseEntity<SecurityEvent> receiveEvent(@RequestBody SecurityEvent event) {
        return ResponseEntity.ok(service.processEvent(event));
    }
}
