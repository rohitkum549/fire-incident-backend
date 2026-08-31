package com.company.firemanagement.domains.shift.repository;

import com.company.firemanagement.domains.shift.entity.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, UUID> {
    Optional<EmployeeShift> findByEmployeeIdAndStatus(UUID employeeId, String status);
}
