package com.company.firemanagement.domains.health.repository;

import com.company.firemanagement.domains.health.entity.HealthCheckLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HealthCheckLogRepository extends JpaRepository<HealthCheckLog, UUID> {
}
