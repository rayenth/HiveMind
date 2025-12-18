package com.security.backend.repository;

import com.security.backend.model.Laptop;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LaptopRepository extends CassandraRepository<Laptop, UUID> {
}
