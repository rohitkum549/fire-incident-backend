package com.company.firemanagement.domains.operations.controller;

import com.company.firemanagement.domains.operations.dto.IncidentRequest;
import com.company.firemanagement.domains.operations.dto.IncidentResponse;
import com.company.firemanagement.domains.operations.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody IncidentRequest request) {
        log.info("REST request to dispatch incident for category: {}", request.getCategoryId());
        IncidentResponse response = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_FIREFIGHTER')")
    public ResponseEntity<IncidentResponse> updateIncidentStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        String notes = request.get("notes");
        log.info("REST request to update incident: {} status to: {}", id, status);
        IncidentResponse response = incidentService.updateIncidentStatus(id, status, notes);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_FIREFIGHTER')")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable UUID id) {
        log.info("REST request to fetch incident: {}", id);
        IncidentResponse response = incidentService.getIncident(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_FIREFIGHTER')")
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        log.info("REST request to list all incidents");
        List<IncidentResponse> list = incidentService.getAllIncidents();
        return ResponseEntity.ok(list);
    }
}
