package com.company.firemanagement.domains.shift.dto;

import com.company.firemanagement.domains.shift.entity.EmployeeShift;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ShiftResponse {
    private UUID id;

    @JsonProperty("employee_id")
    private UUID employeeId;

    @JsonProperty("station_id")
    private UUID stationId;

    @JsonProperty("check_in_time")
    private Instant checkInTime;

    @JsonProperty("check_out_time")
    private Instant checkOutTime;

    @JsonProperty("check_in_latitude")
    private BigDecimal checkInLatitude;

    @JsonProperty("check_in_longitude")
    private BigDecimal checkInLongitude;

    @JsonProperty("check_out_latitude")
    private BigDecimal checkOutLatitude;

    @JsonProperty("check_out_longitude")
    private BigDecimal checkOutLongitude;

    private String status;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static ShiftResponse from(EmployeeShift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .employeeId(shift.getEmployee().getId())
                .stationId(shift.getStation().getId())
                .checkInTime(shift.getCheckInTime())
                .checkOutTime(shift.getCheckOutTime())
                .checkInLatitude(shift.getCheckInLatitude())
                .checkInLongitude(shift.getCheckInLongitude())
                .checkOutLatitude(shift.getCheckOutLatitude())
                .checkOutLongitude(shift.getCheckOutLongitude())
                .status(shift.getStatus())
                .createdAt(shift.getCreatedAt())
                .updatedAt(shift.getUpdatedAt())
                .build();
    }
}
