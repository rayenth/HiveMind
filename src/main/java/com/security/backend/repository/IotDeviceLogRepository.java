package com.security.backend.repository;

import com.security.backend.model.IotDeviceLog;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface IotDeviceLogRepository extends CassandraRepository<IotDeviceLog, UUID> {
    List<IotDeviceLog> findByDeviceId(UUID deviceId);
}
