package com.security.backend.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("security_events")
@Data
@AllArgsConstructor
public class SecurityEvent {
    @PrimaryKey
    private UUID id;

    private String eventType;
    private String deviceId;
    private String severity;
    private String username;
    private String authenticationStatus;

    private LocalDateTime timestamp;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public SecurityEvent() {
        this.id = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
