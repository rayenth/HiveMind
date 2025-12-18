package com.security.backend.controller;

import com.security.backend.model.Alert;
import com.security.backend.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend access
public class AlertController {

    private final AlertService service;

    @GetMapping
    public List<Alert> getAllAlerts() {
        return service.getAllAlerts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable UUID id) {
        Alert alert = service.getAlertById(id);
        return alert != null ? ResponseEntity.ok(alert) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Alert createAlert(@RequestBody Alert alert) {
        return service.createAlert(alert);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Alert> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        Alert updated = service.updateAlertStatus(id, status);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @GetMapping("/critical")
    public List<Alert> getCriticalAlerts() {
        return service.getCriticalAlerts();
    }
}
