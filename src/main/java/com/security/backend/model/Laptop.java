package com.security.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("laptops")
@Data
@AllArgsConstructor
public class Laptop {
    @PrimaryKey
    private UUID id;

    @Column("mac_address")
    private String macAddress;

    private String name;
    private String ipAddress;
    private String status; // ONLINE, OFFLINE, COMPROMISED

    @Column("os_version")
    private String osVersion;

    private String owner;

    private LocalDateTime lastSeen;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public Laptop() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
