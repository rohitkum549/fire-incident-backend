package com.company.firemanagement.domains.geography.repository;

import com.company.firemanagement.domains.geography.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {

    Optional<Country> findByName(String name);

    Optional<Country> findByIsoCode(String isoCode);
}
