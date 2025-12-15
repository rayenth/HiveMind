package com.security.backend.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("alerts")
@Data
@AllArgsConstructor
public class Alert {
    @PrimaryKey
    private UUID id;

    private String type;
    private String message;
    private String severity;
    private String status;
    private LocalDateTime timestamp;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public Alert() {
        this.id = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
