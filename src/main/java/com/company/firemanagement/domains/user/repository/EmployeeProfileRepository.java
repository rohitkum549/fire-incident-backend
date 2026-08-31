package com.company.firemanagement.domains.user.repository;

import com.company.firemanagement.domains.user.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, UUID> {
    Optional<EmployeeProfile> findByEmployeeCode(String employeeCode);
}
