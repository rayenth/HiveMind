package com.security.backend.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("services")
@Data
@AllArgsConstructor
public class Service {
    @PrimaryKey
    private UUID id;

    private String name;
    private String status;
    private int port;
    private String version;

    @Column("timestart")
    private LocalDateTime timeStart;

    @Column("timeend")
    private LocalDateTime timeEnd;

    public Service() {
        this.id = UUID.randomUUID();
        this.timeStart = LocalDateTime.now();
    }
}
