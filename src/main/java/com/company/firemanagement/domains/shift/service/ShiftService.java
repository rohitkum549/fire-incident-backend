package com.company.firemanagement.domains.shift.service;

import com.company.firemanagement.common.exception.BaseException;
import com.company.firemanagement.common.exception.ErrorCode;
import com.company.firemanagement.domains.geography.entity.FireStation;
import com.company.firemanagement.domains.geography.repository.FireStationRepository;
import com.company.firemanagement.domains.shift.dto.CheckInRequest;
import com.company.firemanagement.domains.shift.dto.CheckOutRequest;
import com.company.firemanagement.domains.shift.dto.ShiftResponse;
import com.company.firemanagement.domains.shift.entity.EmployeeShift;
import com.company.firemanagement.domains.shift.repository.EmployeeShiftRepository;
import com.company.firemanagement.domains.user.entity.User;
import com.company.firemanagement.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftService {

    private final EmployeeShiftRepository employeeShiftRepository;
    private final UserRepository userRepository;
    private final FireStationRepository fireStationRepository;

    private static final double GEOFENCE_RADIUS_METERS = 500.0;

    @Transactional
    public ShiftResponse checkIn(CheckInRequest request) {
        log.info("Processing check-in for employee: {} at station: {}", request.getEmployeeId(), request.getStationId());

        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Employee not found with ID: " + request.getEmployeeId()));

        FireStation station = fireStationRepository.findById(request.getStationId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Fire station not found with ID: " + request.getStationId()));

        // Ensure no active shift exists
        employeeShiftRepository.findByEmployeeIdAndStatus(request.getEmployeeId(), "ACTIVE")
                .ifPresent(s -> {
                    throw new BaseException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Employee already has an active shift");
                });

        EmployeeShift shift = new EmployeeShift();
        shift.setEmployee(employee);
        shift.setStation(station);
        shift.setCheckInLatitude(request.getCheckInLatitude());
        shift.setCheckInLongitude(request.getCheckInLongitude());
        shift.setCheckInTime(Instant.now());
        shift.setStatus("ACTIVE");

        EmployeeShift saved = employeeShiftRepository.save(shift);
        return ShiftResponse.from(saved);
    }

    @Transactional
    public ShiftResponse checkOut(CheckOutRequest request) {
        log.info("Processing check-out for shift ID: {}", request.getShiftId());

        EmployeeShift shift = employeeShiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Active shift not found with ID: " + request.getShiftId()));

        if (!"ACTIVE".equals(shift.getStatus())) {
            throw new BaseException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Shift is not active, current status: " + shift.getStatus());
        }

        FireStation station = shift.getStation();

        // Calculate distance from station physical coordinates
        double distance = calculateDistance(
                station.getLatitude(), station.getLongitude(),
                request.getCheckOutLatitude(), request.getCheckOutLongitude()
        );

        log.info("Calculated check-out distance: {} meters from station: {}", distance, station.getName());

        shift.setCheckOutLatitude(request.getCheckOutLatitude());
        shift.setCheckOutLongitude(request.getCheckOutLongitude());
        shift.setCheckOutTime(Instant.now());
        
        if (distance <= GEOFENCE_RADIUS_METERS) {
            shift.setStatus("COMPLETED");
        } else {
            log.warn("Shift check-out outside the 500m geofence. Setting status to ABNORMAL.");
            shift.setStatus("ABNORMAL");
        }

        EmployeeShift saved = employeeShiftRepository.save(shift);
        return ShiftResponse.from(saved);
    }

    /**
     * Calculates distance in meters between two geolocations using the Haversine formula.
     */
    private double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        final int R = 6371000; // Radius of the Earth in meters
        
        double latDistance = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double lonDistance = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
