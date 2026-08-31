package com.company.firemanagement.domains.geography;

import com.company.firemanagement.BaseIntegrationTest;
import com.company.firemanagement.common.exception.BaseException;
import com.company.firemanagement.common.exception.ErrorCode;
import com.company.firemanagement.domains.geography.entity.City;
import com.company.firemanagement.domains.geography.entity.Country;
import com.company.firemanagement.domains.geography.entity.FireStation;
import com.company.firemanagement.domains.geography.entity.State;
import com.company.firemanagement.domains.geography.repository.CityRepository;
import com.company.firemanagement.domains.geography.repository.CountryRepository;
import com.company.firemanagement.domains.geography.repository.FireStationRepository;
import com.company.firemanagement.domains.geography.repository.StateRepository;
import com.company.firemanagement.domains.geography.service.FireStationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FireStationServiceIT extends BaseIntegrationTest {

    @Autowired
    private FireStationService fireStationService;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private FireStationRepository fireStationRepository;

    @BeforeEach
    public void setUp() {
        cleanDatabase();
    }

    @Test
    public void testCreateGeographyAndStation() {
        // 1. Create Country
        Country country = fireStationService.createCountry("United States", "USA");
        assertThat(country.getId()).isNotNull();
        assertThat(country.getName()).isEqualTo("United States");
        assertThat(country.getIsoCode()).isEqualTo("USA");

        // 2. Create State
        State state = fireStationService.createState(country.getId(), "California", "CA");
        assertThat(state.getId()).isNotNull();
        assertThat(state.getName()).isEqualTo("California");
        assertThat(state.getCode()).isEqualTo("CA");
        assertThat(state.getCountry().getId()).isEqualTo(country.getId());

        // 3. Create City
        City city = fireStationService.createCity(state.getId(), "San Francisco");
        assertThat(city.getId()).isNotNull();
        assertThat(city.getName()).isEqualTo("San Francisco");
        assertThat(city.getState().getId()).isEqualTo(state.getId());

        // 4. Create FireStation
        FireStation station = fireStationService.createFireStation(
                city.getId(),
                "SFFD Station 1",
                "251 Lafayette St, San Francisco",
                new BigDecimal("37.774900"),
                new BigDecimal("-122.419400")
        );
        assertThat(station.getId()).isNotNull();
        assertThat(station.getName()).isEqualTo("SFFD Station 1");
        assertThat(station.getAddress()).isEqualTo("251 Lafayette St, San Francisco");
        assertThat(station.getLatitude()).isEqualByComparingTo("37.774900");
        assertThat(station.getLongitude()).isEqualByComparingTo("-122.419400");
        assertThat(station.getStatus()).isEqualTo("ACTIVE");

        // 5. Query
        FireStation fetched = fireStationService.getFireStation(station.getId());
        assertThat(fetched.getName()).isEqualTo("SFFD Station 1");

        List<FireStation> list = fireStationService.getAllFireStations();
        assertThat(list).hasSize(1);
    }

    @Test
    public void testDuplicateCountryIsoCodeThrowsConflictException() {
        fireStationService.createCountry("United States", "USA");

        assertThatThrownBy(() -> fireStationService.createCountry("United States Duplicate", "USA"))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> {
                    BaseException baseEx = (BaseException) ex;
                    assertThat(baseEx.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
                    assertThat(baseEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    public void testCityNotFoundThrowsResourceNotFoundException() {
        UUID randomId = UUID.randomUUID();
        assertThatThrownBy(() -> fireStationService.createFireStation(
                randomId,
                "SFFD Station 1",
                "Address",
                new BigDecimal("37.7749"),
                new BigDecimal("-122.4194")
        ))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> {
                    BaseException baseEx = (BaseException) ex;
                    assertThat(baseEx.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
                    assertThat(baseEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }
}
