package com.company.firemanagement.domains.operations.dto;

import com.company.firemanagement.domains.operations.entity.Complaint;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ComplaintResponse {
    private UUID id;
    private UUID reporterId;
    private String reporterName;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String severity;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static ComplaintResponse from(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .reporterId(complaint.getReporter() != null ? complaint.getReporter().getId() : null)
                .reporterName(complaint.getReporter() != null ? complaint.getReporter().getUsername() : null)
                .categoryId(complaint.getCategory().getId())
                .categoryName(complaint.getCategory().getName())
                .latitude(complaint.getLatitude())
                .longitude(complaint.getLongitude())
                .severity(complaint.getSeverity())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .build();
    }
}
