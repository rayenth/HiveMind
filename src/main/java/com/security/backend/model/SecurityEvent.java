package com.security.backend.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Column;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("security_events")
public class SecurityEvent {

    @PrimaryKey
    private UUID eventId;

    @Column("event_type")
    private String eventType;

    @Column("device_id")
    private String deviceId;

    private String severity;
    private LocalDateTime timestamp;
    private String metadata;

    public SecurityEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "SecurityEvent{" +
                "eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", severity='" + severity + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
