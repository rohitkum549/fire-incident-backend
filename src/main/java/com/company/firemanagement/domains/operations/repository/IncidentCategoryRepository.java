package com.company.firemanagement.domains.operations.repository;

import com.company.firemanagement.domains.operations.entity.IncidentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentCategoryRepository extends JpaRepository<IncidentCategory, UUID> {
    Optional<IncidentCategory> findByName(String name);
}
