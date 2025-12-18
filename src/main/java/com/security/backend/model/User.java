package com.security.backend.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Table("users")
@Data
@AllArgsConstructor
public class User {
    @PrimaryKey
    private UUID id;

    @Column("username")
    private String username;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column("password")
    private String password;

    private String role; // ADMIN, USER, SECURITY_ENGINEER

    @Column("created_at")
    private java.time.LocalDateTime createdAt;

    @Column("updated_at")
    private java.time.LocalDateTime updatedAt;

    public User() {
        this.id = UUID.randomUUID();
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
