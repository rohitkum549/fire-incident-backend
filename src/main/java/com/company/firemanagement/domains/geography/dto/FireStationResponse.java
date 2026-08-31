package com.company.firemanagement.domains.geography.dto;

import com.company.firemanagement.domains.geography.entity.FireStation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class FireStationResponse {
    private UUID id;
    private UUID cityId;
    private String cityName;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static FireStationResponse from(FireStation station) {
        return FireStationResponse.builder()
                .id(station.getId())
                .cityId(station.getCity().getId())
                .cityName(station.getCity().getName())
                .name(station.getName())
                .address(station.getAddress())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .status(station.getStatus())
                .createdAt(station.getCreatedAt())
                .updatedAt(station.getUpdatedAt())
                .build();
    }
}
