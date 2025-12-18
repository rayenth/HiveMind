package com.security.backend.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Column;
import java.time.LocalDateTime;

@Table("anomaly_alerts")
public class AnomalyAlert {
    @PrimaryKey("alert_id")
    private String alertId;

    @Column("device_id")
    private String deviceId;

    private String description;

    @Column("detected_value")
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
