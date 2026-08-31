package com.company.firemanagement.domains.geography.repository;

import com.company.firemanagement.domains.geography.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends JpaRepository<State, UUID> {
    Optional<State> findByCountryIdAndCode(UUID countryId, String code);
}
