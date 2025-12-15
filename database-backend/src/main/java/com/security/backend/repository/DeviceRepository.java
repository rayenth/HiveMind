package com.security.backend.repository;

import com.security.backend.model.Device;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DeviceRepository extends CassandraRepository<Device, UUID> {
}
