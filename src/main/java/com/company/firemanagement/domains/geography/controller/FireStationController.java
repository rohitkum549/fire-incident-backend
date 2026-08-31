package com.company.firemanagement.domains.geography.controller;

import com.company.firemanagement.domains.geography.dto.FireStationRequest;
import com.company.firemanagement.domains.geography.dto.FireStationResponse;
import com.company.firemanagement.domains.geography.entity.City;
import com.company.firemanagement.domains.geography.entity.Country;
import com.company.firemanagement.domains.geography.entity.FireStation;
import com.company.firemanagement.domains.geography.entity.State;
import com.company.firemanagement.domains.geography.service.FireStationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FireStationController {

    private final FireStationService fireStationService;

    @PostMapping("/geography/countries")
    public ResponseEntity<Country> createCountry(@RequestBody Map<String, String> request) {
        Country country = fireStationService.createCountry(request.get("name"), request.get("iso_code"));
        return ResponseEntity.status(HttpStatus.CREATED).body(country);
    }

    @PostMapping("/geography/states")
    public ResponseEntity<State> createState(@RequestBody Map<String, Object> request) {
        UUID countryId = UUID.fromString(request.get("country_id").toString());
        State state = fireStationService.createState(countryId, request.get("name").toString(), request.get("code").toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(state);
    }

    @PostMapping("/geography/cities")
    public ResponseEntity<City> createCity(@RequestBody Map<String, Object> request) {
        UUID stateId = UUID.fromString(request.get("state_id").toString());
        City city = fireStationService.createCity(stateId, request.get("name").toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(city);
    }

    @PostMapping("/stations")
    public ResponseEntity<FireStationResponse> createFireStation(@Valid @RequestBody FireStationRequest request) {
        log.info("REST request to register fire station: {}", request.getName());
        FireStation station = fireStationService.createFireStation(
                request.getCityId(),
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(FireStationResponse.from(station));
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<FireStationResponse> getFireStation(@PathVariable UUID id) {
        log.info("REST request to fetch fire station: {}", id);
        FireStation station = fireStationService.getFireStation(id);
        return ResponseEntity.ok(FireStationResponse.from(station));
    }

    @GetMapping("/stations")
    public ResponseEntity<List<FireStationResponse>> getAllFireStations() {
        log.info("REST request to list all fire stations");
        List<FireStationResponse> list = fireStationService.getAllFireStations().stream()
                .map(FireStationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
