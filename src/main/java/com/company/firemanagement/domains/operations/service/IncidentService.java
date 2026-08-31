package com.company.firemanagement.domains.operations.service;

import com.company.firemanagement.common.exception.BaseException;
import com.company.firemanagement.common.exception.ErrorCode;
import com.company.firemanagement.domains.geography.entity.FireStation;
import com.company.firemanagement.domains.geography.repository.FireStationRepository;
import com.company.firemanagement.domains.operations.dto.IncidentRequest;
import com.company.firemanagement.domains.operations.dto.IncidentResponse;
import com.company.firemanagement.domains.operations.entity.Complaint;
import com.company.firemanagement.domains.operations.entity.Incident;
import com.company.firemanagement.domains.operations.entity.IncidentCategory;
import com.company.firemanagement.domains.operations.repository.ComplaintRepository;
import com.company.firemanagement.domains.operations.repository.IncidentCategoryRepository;
import com.company.firemanagement.domains.operations.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final ComplaintRepository complaintRepository;
    private final IncidentCategoryRepository categoryRepository;
    private final FireStationRepository fireStationRepository;

    private static final List<String> VALID_STATUSES = Arrays.asList("DISPATCHED", "IN_PROGRESS", "RESOLVED", "CANCELLED");

    @Transactional
    public IncidentResponse createIncident(IncidentRequest request) {
        log.info("Creating incident for category: {} responding station: {}", request.getCategoryId(), request.getStationId());

        Complaint complaint = null;
        if (request.getComplaintId() != null) {
            complaint = complaintRepository.findById(request.getComplaintId())
                    .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Complaint not found with ID: " + request.getComplaintId()));
            
            if ("APPROVED".equals(complaint.getStatus())) {
                throw new BaseException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Complaint has already been escalated to an incident");
            }
            complaint.setStatus("APPROVED");
            complaintRepository.save(complaint);
        }

        FireStation station = fireStationRepository.findById(request.getStationId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Fire station not found with ID: " + request.getStationId()));

        IncidentCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Incident category not found with ID: " + request.getCategoryId()));

        Incident incident = new Incident();
        incident.setComplaint(complaint);
        incident.setStation(station);
        incident.setCategory(category);
        incident.setStatus("DISPATCHED");
        incident.setSeverity(request.getSeverity().toUpperCase());
        incident.setLatitude(request.getLatitude());
        incident.setLongitude(request.getLongitude());
        incident.setDispatchedAt(Instant.now());
        incident.setNotes(request.getNotes());

        Incident saved = incidentRepository.save(incident);
        return IncidentResponse.from(saved);
    }

    @Transactional
    public IncidentResponse updateIncidentStatus(UUID id, String status, String notes) {
        log.info("Updating status of incident: {} to status: {}", id, status);

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Incident not found with ID: " + id));

        String upperStatus = status.toUpperCase();
        if (!VALID_STATUSES.contains(upperStatus)) {
            throw new BaseException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Invalid incident status: " + status + ". Allowed statuses: " + VALID_STATUSES);
        }

        incident.setStatus(upperStatus);
        if (notes != null) {
            incident.setNotes(notes);
        }

        if ("RESOLVED".equals(upperStatus)) {
            incident.setResolvedAt(Instant.now());
        }

        Incident saved = incidentRepository.save(incident);
        return IncidentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Incident not found with ID: " + id));
        return IncidentResponse.from(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(IncidentResponse::from)
                .collect(Collectors.toList());
    }
}
