package com.company.firemanagement.domains.operations.repository;

import com.company.firemanagement.domains.operations.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {
}
