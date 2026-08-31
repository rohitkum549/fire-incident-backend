package com.company.firemanagement.domains.geography.repository;

import com.company.firemanagement.domains.geography.entity.FireStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FireStationRepository extends JpaRepository<FireStation, UUID> {
}
