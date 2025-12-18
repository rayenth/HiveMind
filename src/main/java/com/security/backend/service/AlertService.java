package com.security.backend.service;

import com.security.backend.model.Alert;
import com.security.backend.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;

    public List<Alert> getAllAlerts() {
        return repository.findAll();
    }

    public Alert getAlertById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public Alert createAlert(Alert alert) {
        if (alert.getId() == null) {
            alert.setId(UUID.randomUUID());
        }
        if (alert.getTimestamp() == null) {
            alert.setTimestamp(LocalDateTime.now());
        }
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        return repository.save(alert);
    }

    public Alert updateAlertStatus(UUID id, String status) {
        Alert alert = getAlertById(id);
        if (alert != null) {
            alert.setStatus(status);
            alert.setUpdatedAt(LocalDateTime.now());
            return repository.save(alert);
        }
        return null;
    }

    public List<Alert> getCriticalAlerts() {
        return repository.findAll().stream()
                .filter(a -> "CRITICAL".equalsIgnoreCase(a.getSeverity()))
                .collect(Collectors.toList());
    }
}
