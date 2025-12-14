package com.iot.anomaly.controller;

import com.iot.anomaly.model.AnomalyAlert;
import com.iot.anomaly.service.AnomalyDetectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnomalyController {

    @Autowired
    private AnomalyDetectorService anomalyDetectorService;

    @GetMapping("/status")
    public String getStatus() {
        return "Anomaly Detection Service is UP";
    }

    @GetMapping("/anomalies")
    public List<AnomalyAlert> getAnomalies() {
        return anomalyDetectorService.getRecentAnomalies();
    }
}
