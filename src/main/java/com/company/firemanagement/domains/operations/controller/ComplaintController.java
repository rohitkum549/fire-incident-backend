package com.company.firemanagement.domains.operations.controller;

import com.company.firemanagement.domains.operations.dto.ComplaintRequest;
import com.company.firemanagement.domains.operations.dto.ComplaintResponse;
import com.company.firemanagement.domains.operations.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(@Valid @RequestBody ComplaintRequest request) {
        log.info("REST request to submit complaint from reporter: {}", request.getReporterId());
        ComplaintResponse response = complaintService.createComplaint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getComplaint(@PathVariable UUID id) {
        log.info("REST request to fetch complaint: {}", id);
        ComplaintResponse response = complaintService.getComplaint(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints() {
        log.info("REST request to list all complaints");
        List<ComplaintResponse> list = complaintService.getAllComplaints();
        return ResponseEntity.ok(list);
    }
}
