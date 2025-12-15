package com.security.backend.repository;

import com.security.backend.model.SecurityEvent;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityEventRepository extends CassandraRepository<SecurityEvent, UUID> {
}
