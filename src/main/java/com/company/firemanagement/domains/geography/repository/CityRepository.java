package com.company.firemanagement.domains.geography.repository;

import com.company.firemanagement.domains.geography.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {
    Optional<City> findByStateIdAndName(UUID stateId, String name);
}
