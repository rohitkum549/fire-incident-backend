package com.company.firemanagement.domains.geography.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FireStationService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final FireStationRepository fireStationRepository;

    @Transactional
    public Country createCountry(String name, String isoCode) {
        log.info("Creating country: {} with ISO code: {}", name, isoCode);
        if (countryRepository.findByIsoCode(isoCode).isPresent()) {
            throw new BaseException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Country with ISO code " + isoCode + " already exists");
        }
        Country country = new Country();
        country.setName(name);
        country.setIsoCode(isoCode);
        return countryRepository.save(country);
    }

    @Transactional
    public State createState(UUID countryId, String name, String code) {
        log.info("Creating state: {} with code: {} in country: {}", name, code, countryId);
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Country not found with ID: " + countryId));
        
        if (stateRepository.findByCountryIdAndCode(countryId, code).isPresent()) {
            throw new BaseException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "State with code " + code + " already exists in country " + countryId);
        }

        State state = new State();
        state.setCountry(country);
        state.setName(name);
        state.setCode(code);
        return stateRepository.save(state);
    }

    @Transactional
    public City createCity(UUID stateId, String name) {
        log.info("Creating city: {} in state: {}", name, stateId);
        State state = stateRepository.findById(stateId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "State not found with ID: " + stateId));

        if (cityRepository.findByStateIdAndName(stateId, name).isPresent()) {
            throw new BaseException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "City with name " + name + " already exists in state " + stateId);
        }

        City city = new City();
        city.setState(state);
        city.setName(name);
        return cityRepository.save(city);
    }

    @Transactional
    public FireStation createFireStation(UUID cityId, String name, String address, BigDecimal latitude, BigDecimal longitude) {
        log.info("Creating fire station: {} in city: {}", name, cityId);
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "City not found with ID: " + cityId));

        FireStation station = new FireStation();
        station.setCity(city);
        station.setName(name);
        station.setAddress(address);
        station.setLatitude(latitude);
        station.setLongitude(longitude);
        station.setStatus("ACTIVE");
        return fireStationRepository.save(station);
    }

    @Transactional(readOnly = true)
    public FireStation getFireStation(UUID id) {
        return fireStationRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Fire station not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<FireStation> getAllFireStations() {
        return fireStationRepository.findAll();
    }
}
