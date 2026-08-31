package com.company.firemanagement.domains.shift.controller;

import com.company.firemanagement.domains.shift.dto.CheckInRequest;
import com.company.firemanagement.domains.shift.dto.CheckOutRequest;
import com.company.firemanagement.domains.shift.dto.ShiftResponse;
import com.company.firemanagement.domains.shift.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('ROLE_FIREFIGHTER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ShiftResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        log.info("REST request to check-in employee: {}", request.getEmployeeId());
        ShiftResponse response = shiftService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/check-out")
    @PreAuthorize("hasRole('ROLE_FIREFIGHTER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ShiftResponse> checkOut(@Valid @RequestBody CheckOutRequest request) {
        log.info("REST request to check-out shift: {}", request.getShiftId());
        ShiftResponse response = shiftService.checkOut(request);
        return ResponseEntity.ok(response);
    }
}
