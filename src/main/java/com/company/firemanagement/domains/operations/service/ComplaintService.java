package com.company.firemanagement.domains.operations.service;

import com.company.firemanagement.common.exception.BaseException;
import com.company.firemanagement.common.exception.ErrorCode;
import com.company.firemanagement.domains.operations.dto.ComplaintRequest;
import com.company.firemanagement.domains.operations.dto.ComplaintResponse;
import com.company.firemanagement.domains.operations.entity.Complaint;
import com.company.firemanagement.domains.operations.entity.IncidentCategory;
import com.company.firemanagement.domains.operations.repository.ComplaintRepository;
import com.company.firemanagement.domains.operations.repository.IncidentCategoryRepository;
import com.company.firemanagement.domains.user.entity.User;
import com.company.firemanagement.domains.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final IncidentCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @PostConstruct
    @Transactional
    public void initCategories() {
        log.info("Initializing system incident categories...");
        ensureCategory("RESIDENTIAL_FIRE", "Fire incident occurring in residential buildings");
        ensureCategory("WILDFIRE", "Fire incident occurring in forests, grasslands, or fields");
        ensureCategory("HAZMAT", "Incident involving hazardous chemicals or spills");
        ensureCategory("MEDICAL_EMERGENCY", "Medical assistance required along with fire responders");
    }

    private void ensureCategory(String name, String description) {
        if (categoryRepository.findByName(name).isEmpty()) {
            IncidentCategory category = new IncidentCategory();
            category.setName(name);
            category.setDescription(description);
            categoryRepository.save(category);
        }
    }

    @Transactional
    public IncidentCategory createCategory(String name, String description) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new BaseException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Category already exists: " + name);
        }
        IncidentCategory category = new IncidentCategory();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request) {
        log.info("Creating complaint from reporter: {}", request.getReporterId());

        User reporter = userRepository.findById(request.getReporterId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Reporter not found with ID: " + request.getReporterId()));

        IncidentCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Incident category not found with ID: " + request.getCategoryId()));

        Complaint complaint = new Complaint();
        complaint.setReporter(reporter);
        complaint.setCategory(category);
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setSeverity(request.getSeverity().toUpperCase());
        complaint.setDescription(request.getDescription());
        complaint.setStatus("PENDING");

        Complaint saved = complaintRepository.save(complaint);
        return ComplaintResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Complaint getComplaintEntity(UUID id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Complaint not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaint(UUID id) {
        return ComplaintResponse.from(getComplaintEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(ComplaintResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidentCategory> getAllCategories() {
        return categoryRepository.findAll();
    }
}
