package com.company.firemanagement.domains.operations.dto;

import com.company.firemanagement.domains.operations.entity.Incident;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class IncidentResponse {
    private UUID id;
    private UUID complaintId;
    private UUID stationId;
    private String stationName;
    private UUID categoryId;
    private String categoryName;
    private String status;
    private String severity;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Instant dispatchedAt;
    private Instant resolvedAt;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public static IncidentResponse from(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .complaintId(incident.getComplaint() != null ? incident.getComplaint().getId() : null)
                .stationId(incident.getStation().getId())
                .stationName(incident.getStation().getName())
                .categoryId(incident.getCategory().getId())
                .categoryName(incident.getCategory().getName())
                .status(incident.getStatus())
                .severity(incident.getSeverity())
                .latitude(incident.getLatitude())
                .longitude(incident.getLongitude())
                .dispatchedAt(incident.getDispatchedAt())
                .resolvedAt(incident.getResolvedAt())
                .notes(incident.getNotes())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}
