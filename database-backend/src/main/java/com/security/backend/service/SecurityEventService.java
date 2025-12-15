package com.security.backend.service;

import com.security.backend.model.SecurityEvent;
import com.security.backend.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityEventService {

    private final SecurityEventRepository repository;

    public SecurityEvent processEvent(SecurityEvent event) {
        // Here we could add logic to forward to AI or trigger alerts
        return repository.save(event);
    }
}
