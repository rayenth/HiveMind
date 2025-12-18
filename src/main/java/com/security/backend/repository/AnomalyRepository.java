package com.security.backend.repository;

import com.security.backend.model.AnomalyAlert;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnomalyRepository extends CassandraRepository<AnomalyAlert, String> {
}
