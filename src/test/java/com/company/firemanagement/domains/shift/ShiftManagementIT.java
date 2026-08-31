package com.company.firemanagement.domains.shift;

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
import com.company.firemanagement.domains.shift.dto.CheckInRequest;
import com.company.firemanagement.domains.shift.dto.CheckOutRequest;
import com.company.firemanagement.domains.shift.dto.ShiftResponse;
import com.company.firemanagement.domains.shift.repository.EmployeeShiftRepository;
import com.company.firemanagement.domains.shift.service.ShiftService;
import com.company.firemanagement.domains.user.dto.LoginRequest;
import com.company.firemanagement.domains.user.dto.LoginResponse;
import com.company.firemanagement.domains.user.dto.RegisterRequest;
import com.company.firemanagement.domains.user.dto.RegisterResponse;
import com.company.firemanagement.domains.user.repository.EmployeeProfileRepository;
import com.company.firemanagement.domains.user.repository.UserRepository;
import com.company.firemanagement.domains.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ShiftManagementIT extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private FireStationService fireStationService;

    @Autowired
    private EmployeeShiftRepository employeeShiftRepository;

    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FireStationRepository fireStationRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CountryRepository countryRepository;

    private FireStation station;

    @BeforeEach
    public void setUp() {
        employeeShiftRepository.deleteAll();
        employeeProfileRepository.deleteAll();
        userRepository.deleteAll();
        fireStationRepository.deleteAll();
        cityRepository.deleteAll();
        stateRepository.deleteAll();
        countryRepository.deleteAll();

        // Setup base geography & station
        Country country = fireStationService.createCountry("United States", "USA");
        State state = fireStationService.createState(country.getId(), "California", "CA");
        City city = fireStationService.createCity(state.getId(), "San Francisco");
        
        station = fireStationService.createFireStation(
                city.getId(),
                "SFFD Station 1",
                "251 Lafayette St, San Francisco",
                new BigDecimal("37.774900"),
                new BigDecimal("-122.419400")
        );
    }

    @Test
    public void testRegistrationLoginCheckInAndCheckOut() {
        // 1. Register Firefighter
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername("firefighter_bob");
        registerReq.setEmail("bob@example.com");
        registerReq.setPassword("Password123!");
        registerReq.setFirstName("Bob");
        registerReq.setLastName("Builder");
        registerReq.setRoleNames(List.of("ROLE_FIREFIGHTER"));
        registerReq.setStationId(station.getId());
        registerReq.setEmployeeCode("EMP-99881");

        RegisterResponse regResponse = userService.registerUser(registerReq);
        assertThat(regResponse.getId()).isNotNull();
        assertThat(regResponse.getUsername()).isEqualTo("firefighter_bob");

        // 2. Login
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("firefighter_bob");
        loginReq.setPassword("Password123!");
        
        LoginResponse loginResponse = userService.loginUser(loginReq);
        assertThat(loginResponse.getAccessToken()).isNotBlank();
        assertThat(loginResponse.getRoles()).contains("ROLE_FIREFIGHTER");

        // 3. Shift Check-in (Exactly at the station coordinates)
        CheckInRequest checkInReq = new CheckInRequest();
        checkInReq.setEmployeeId(regResponse.getId());
        checkInReq.setStationId(station.getId());
        checkInReq.setCheckInLatitude(new BigDecimal("37.774900"));
        checkInReq.setCheckInLongitude(new BigDecimal("-122.419400"));

        ShiftResponse shiftResponse = shiftService.checkIn(checkInReq);
        assertThat(shiftResponse.getId()).isNotNull();
        assertThat(shiftResponse.getStatus()).isEqualTo("ACTIVE");
        assertThat(shiftResponse.getEmployeeId()).isEqualTo(regResponse.getId());

        // 4. Duplicate Check-in should fail
        assertThatThrownBy(() -> shiftService.checkIn(checkInReq))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> {
                    BaseException baseEx = (BaseException) ex;
                    assertThat(baseEx.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
                    assertThat(baseEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });

        // 5. Shift Check-out (Close enough - within 100 meters)
        CheckOutRequest checkOutReq = new CheckOutRequest();
        checkOutReq.setShiftId(shiftResponse.getId());
        // Coordinates slightly displaced (approx 80m away)
        checkOutReq.setCheckOutLatitude(new BigDecimal("37.775600"));
        checkOutReq.setCheckOutLongitude(new BigDecimal("-122.419400"));

        ShiftResponse completedShift = shiftService.checkOut(checkOutReq);
        assertThat(completedShift.getStatus()).isEqualTo("COMPLETED");
        assertThat(completedShift.getCheckOutTime()).isNotNull();
    }

    @Test
    public void testAbnormalCheckOutOutsideGeofence() {
        // Register Firefighter
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername("firefighter_tim");
        registerReq.setEmail("tim@example.com");
        registerReq.setPassword("Password123!");
        registerReq.setFirstName("Tim");
        registerReq.setLastName("Tester");
        registerReq.setRoleNames(List.of("ROLE_FIREFIGHTER"));
        registerReq.setStationId(station.getId());
        registerReq.setEmployeeCode("EMP-12345");

        RegisterResponse regResponse = userService.registerUser(registerReq);

        // Check-in
        CheckInRequest checkInReq = new CheckInRequest();
        checkInReq.setEmployeeId(regResponse.getId());
        checkInReq.setStationId(station.getId());
        checkInReq.setCheckInLatitude(new BigDecimal("37.774900"));
        checkInReq.setCheckInLongitude(new BigDecimal("-122.419400"));
        ShiftResponse shiftResponse = shiftService.checkIn(checkInReq);

        // Check-out (Far away - over 100km away)
        CheckOutRequest checkOutReq = new CheckOutRequest();
        checkOutReq.setShiftId(shiftResponse.getId());
        checkOutReq.setCheckOutLatitude(new BigDecimal("39.774900")); // displaced by 2 degrees lat
        checkOutReq.setCheckOutLongitude(new BigDecimal("-122.419400"));

        ShiftResponse completedShift = shiftService.checkOut(checkOutReq);
        assertThat(completedShift.getStatus()).isEqualTo("ABNORMAL");
    }
}
