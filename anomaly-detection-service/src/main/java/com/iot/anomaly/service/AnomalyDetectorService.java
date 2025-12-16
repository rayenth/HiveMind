package com.iot.anomaly.service;

import com.iot.anomaly.model.DeviceLog;
import com.iot.anomaly.model.DeviceStatus;
import org.springframework.stereotype.Service;

@Service
public class AnomalyDetectorService {

    private static final double MAX_TEMP_THRESHOLD = 100.0;
    private static final double MIN_TEMP_THRESHOLD = -20.0;
    private final java.util.List<com.iot.anomaly.model.AnomalyAlert> recentAnomalies = new java.util.concurrent.CopyOnWriteArrayList<>();

    public boolean isAnomaly(DeviceLog log) {
        if (log == null) {
            return false;
        }
        //check if the temperature is out of bounds
        boolean tempAnomaly = log.temperature() > MAX_TEMP_THRESHOLD || log.temperature() < MIN_TEMP_THRESHOLD;

        //check if the device is offline but sending data
        boolean statusAnomaly = log.status() == DeviceStatus.OFFLINE;
        
        return tempAnomaly || statusAnomaly;
    }

    public String getAnomalyDescription(DeviceLog log) {
        if (log.temperature() > MAX_TEMP_THRESHOLD) {
            return "Temperature too high: " + log.temperature();
        } else if (log.temperature() < MIN_TEMP_THRESHOLD) {
            return "Temperature too low: " + log.temperature();
        }

        if (log.status() == DeviceStatus.OFFLINE) {
            return "Device is offline but sending data !!";
        }
        return "Unknown anomaly";
    }

    public void addAnomaly(com.iot.anomaly.model.AnomalyAlert alert) {
        recentAnomalies.add(0, alert);
        if (recentAnomalies.size() > 100) {
            recentAnomalies.remove(recentAnomalies.size() - 1);
        }
    }

    public java.util.List<com.iot.anomaly.model.AnomalyAlert> getRecentAnomalies() {
        return recentAnomalies;
    }
}
