package com.company.firemanagement.domains.health.controller;

import com.company.firemanagement.domains.health.entity.HealthCheckLog;
import com.company.firemanagement.domains.health.repository.HealthCheckLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Application E2E verification endpoints")
public class HealthCheckController {

    private final HealthCheckLogRepository healthCheckLogRepository;

    // Explicit constructor injection
    public HealthCheckController(HealthCheckLogRepository healthCheckLogRepository) {
        this.healthCheckLogRepository = healthCheckLogRepository;
    }

    @GetMapping
    @Transactional
    @Operation(summary = "Perform complete E2E health check", description = "Validates REST API routing, database transactions, reads/writes, and entity auditing.")
    public ResponseEntity<Map<String, Object>> performHealthCheck() {
        log.info("Processing E2E database connection check...");

        UUID logId = UUID.randomUUID();
        HealthCheckLog checkLog = new HealthCheckLog();
        checkLog.setId(logId);
        checkLog.setStatus("OK");
        checkLog.setCheckedAt(Instant.now());

        // Perform write to Supabase / database
        HealthCheckLog saved = healthCheckLogRepository.save(checkLog);

        // Perform read to verify record insertion
        HealthCheckLog retrieved = healthCheckLogRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalStateException("Database transaction check failed. Record not found."));

        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("database", "CONNECTED");
        Map<String, Object> auditMap = new HashMap<>();
        auditMap.put("recordId", retrieved.getId());
        auditMap.put("status", retrieved.getStatus());
        auditMap.put("checkedAt", retrieved.getCheckedAt());
        auditMap.put("createdAt", retrieved.getCreatedAt());
        auditMap.put("createdBy", retrieved.getCreatedBy() != null ? retrieved.getCreatedBy() : "SYSTEM");
        auditMap.put("updatedAt", retrieved.getUpdatedAt());
        auditMap.put("updatedBy", retrieved.getUpdatedBy() != null ? retrieved.getUpdatedBy() : "SYSTEM");

        result.put("verifiedAudit", auditMap);

        log.info("E2E database check succeeded for ID: {}", logId);
        return ResponseEntity.ok(result);
    }
}
