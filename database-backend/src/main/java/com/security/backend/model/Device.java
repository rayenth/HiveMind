package com.security.backend.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("devices")
@Data
@AllArgsConstructor
public class Device {
    @PrimaryKey
    private UUID id;

    @Column("mac_address")
    private String macAddress;

    private String name;
    private String type; // ROUTER, SERVER, IOT
    private String ipAddress;
    private String status; // ONLINE, OFFLINE, COMPROMISED

    private LocalDateTime lastSeen;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public Device() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
