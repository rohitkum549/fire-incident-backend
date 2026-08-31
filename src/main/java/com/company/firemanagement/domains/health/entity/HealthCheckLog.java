package com.company.firemanagement.domains.health.entity;

import com.company.firemanagement.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "health_check_log")
@Getter
@Setter
public class HealthCheckLog extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Column(name = "status", nullable = false, length = 50)
    private String status;
}
