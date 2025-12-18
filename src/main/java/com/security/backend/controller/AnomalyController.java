package com.security.backend.controller;

import com.security.backend.model.AnomalyAlert;
import com.security.backend.repository.AnomalyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnomalyController {

    private final AnomalyRepository repository;

    @GetMapping
    public List<AnomalyAlert> getAllAnomalies() {
        return repository.findAll();
    }

    @PostMapping
    public AnomalyAlert reportAnomaly(@RequestBody AnomalyAlert anomaly) {
        if (anomaly.getAlertId() == null) {
            anomaly.setAlertId(UUID.randomUUID().toString());
        }
        if (anomaly.getTimestamp() == null) {
            anomaly.setTimestamp(LocalDateTime.now());
        }
        return repository.save(anomaly);
    }
}
