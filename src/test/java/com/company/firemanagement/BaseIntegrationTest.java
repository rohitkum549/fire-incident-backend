package com.company.firemanagement;

import com.company.firemanagement.domains.geography.repository.CityRepository;
import com.company.firemanagement.domains.geography.repository.CountryRepository;
import com.company.firemanagement.domains.geography.repository.FireStationRepository;
import com.company.firemanagement.domains.geography.repository.StateRepository;
import com.company.firemanagement.domains.operations.repository.ComplaintRepository;
import com.company.firemanagement.domains.operations.repository.IncidentCategoryRepository;
import com.company.firemanagement.domains.operations.repository.IncidentRepository;
import com.company.firemanagement.domains.shift.repository.EmployeeShiftRepository;
import com.company.firemanagement.domains.user.repository.EmployeeProfileRepository;
import com.company.firemanagement.domains.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected IncidentRepository incidentRepository;

    @Autowired
    protected ComplaintRepository complaintRepository;

    @Autowired
    protected EmployeeShiftRepository employeeShiftRepository;

    @Autowired
    protected EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected FireStationRepository fireStationRepository;

    @Autowired
    protected CityRepository cityRepository;

    @Autowired
    protected StateRepository stateRepository;

    @Autowired
    protected CountryRepository countryRepository;

    @Autowired
    protected IncidentCategoryRepository categoryRepository;

    protected void cleanDatabase() {
        incidentRepository.deleteAll();
        complaintRepository.deleteAll();
        employeeShiftRepository.deleteAll();
        employeeProfileRepository.deleteAll();
        userRepository.deleteAll();
        fireStationRepository.deleteAll();
        cityRepository.deleteAll();
        stateRepository.deleteAll();
        countryRepository.deleteAll();
        categoryRepository.deleteAll();
    }
}


