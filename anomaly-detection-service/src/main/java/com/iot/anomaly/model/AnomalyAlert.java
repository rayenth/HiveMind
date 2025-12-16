package com.iot.anomaly.model;

import java.time.LocalDateTime;

public class AnomalyAlert {
    private String alertId;
    private String deviceId;
    private String description;
    private double detectedValue;
    private LocalDateTime timestamp;

    public AnomalyAlert() {
    }

    public AnomalyAlert(String alertId, String deviceId, String description, double detectedValue,
            LocalDateTime timestamp) {
        this.alertId = alertId;
        this.deviceId = deviceId;
        this.description = description;
        this.detectedValue = detectedValue;
        this.timestamp = timestamp;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDetectedValue() {
        return detectedValue;
    }

    public void setDetectedValue(double detectedValue) {
        this.detectedValue = detectedValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AnomalyAlert{" +
                "alertId='" + alertId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", description='" + description + '\'' +
                ", detectedValue=" + detectedValue +
                ", timestamp=" + timestamp +
                '}';
    }
}
