package com.company.firemanagement.domains.operations;

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
import com.company.firemanagement.domains.operations.dto.ComplaintRequest;
import com.company.firemanagement.domains.operations.dto.ComplaintResponse;
import com.company.firemanagement.domains.operations.dto.IncidentRequest;
import com.company.firemanagement.domains.operations.dto.IncidentResponse;
import com.company.firemanagement.domains.operations.entity.Complaint;
import com.company.firemanagement.domains.operations.entity.IncidentCategory;
import com.company.firemanagement.domains.operations.repository.ComplaintRepository;
import com.company.firemanagement.domains.operations.repository.IncidentCategoryRepository;
import com.company.firemanagement.domains.operations.repository.IncidentRepository;
import com.company.firemanagement.domains.operations.service.ComplaintService;
import com.company.firemanagement.domains.operations.service.IncidentService;
import com.company.firemanagement.domains.user.dto.RegisterRequest;
import com.company.firemanagement.domains.user.dto.RegisterResponse;
import com.company.firemanagement.domains.user.repository.UserRepository;
import com.company.firemanagement.domains.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IncidentLifecycleIT extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private FireStationService fireStationService;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private IncidentCategoryRepository categoryRepository;

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
    private IncidentCategory category;
    private RegisterResponse citizen;

    @BeforeEach
    public void setUp() {
        cleanDatabase();

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

        // Setup category
        category = complaintService.createCategory("RESIDENTIAL_FIRE", "Fire occurring in residential buildings");

        // Setup Citizen
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername("jane_citizen");
        registerReq.setEmail("jane@example.com");
        registerReq.setPassword("Password123!");
        registerReq.setFirstName("Jane");
        registerReq.setLastName("Doe");
        registerReq.setRoleNames(List.of("ROLE_CITIZEN"));
        citizen = userService.registerUser(registerReq);
    }

    @Test
    public void testComplaintEscalationAndLifecycle() {
        // 1. Submit Complaint
        ComplaintRequest complaintReq = new ComplaintRequest();
        complaintReq.setReporterId(citizen.getId());
        complaintReq.setCategoryId(category.getId());
        complaintReq.setLatitude(new BigDecimal("37.774900"));
        complaintReq.setLongitude(new BigDecimal("-122.419400"));
        complaintReq.setSeverity("HIGH");
        complaintReq.setDescription("Large fire in the main lobby");

        ComplaintResponse complaintResponse = complaintService.createComplaint(complaintReq);
        assertThat(complaintResponse.getId()).isNotNull();
        assertThat(complaintResponse.getStatus()).isEqualTo("PENDING");
        assertThat(complaintResponse.getReporterId()).isEqualTo(citizen.getId());

        // 2. Escalate Complaint to Incident
        IncidentRequest incidentReq = new IncidentRequest();
        incidentReq.setComplaintId(complaintResponse.getId());
        incidentReq.setStationId(station.getId());
        incidentReq.setCategoryId(category.getId());
        incidentReq.setSeverity("HIGH");
        incidentReq.setLatitude(new BigDecimal("37.774900"));
        incidentReq.setLongitude(new BigDecimal("-122.419400"));
        incidentReq.setNotes("Dispatched unit from SFFD Station 1");

        IncidentResponse incidentResponse = incidentService.createIncident(incidentReq);
        assertThat(incidentResponse.getId()).isNotNull();
        assertThat(incidentResponse.getStatus()).isEqualTo("DISPATCHED");
        assertThat(incidentResponse.getComplaintId()).isEqualTo(complaintResponse.getId());
        assertThat(incidentResponse.getStationId()).isEqualTo(station.getId());

        // Verify complaint status updated to APPROVED
        Complaint updatedComplaint = complaintService.getComplaintEntity(complaintResponse.getId());
        assertThat(updatedComplaint.getStatus()).isEqualTo("APPROVED");

        // 3. Duplicate escalation should fail
        assertThatThrownBy(() -> incidentService.createIncident(incidentReq))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> {
                    BaseException baseEx = (BaseException) ex;
                    assertThat(baseEx.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
                    assertThat(baseEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });

        // 4. Update status to IN_PROGRESS
        IncidentResponse inProgress = incidentService.updateIncidentStatus(incidentResponse.getId(), "IN_PROGRESS", "Engine arriving on scene");
        assertThat(inProgress.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(inProgress.getNotes()).isEqualTo("Engine arriving on scene");

        // 5. Update status to RESOLVED
        IncidentResponse resolved = incidentService.updateIncidentStatus(incidentResponse.getId(), "RESOLVED", "Fire extinguished successfully");
        assertThat(resolved.getStatus()).isEqualTo("RESOLVED");
        assertThat(resolved.getResolvedAt()).isNotNull();
        assertThat(resolved.getNotes()).isEqualTo("Fire extinguished successfully");
    }
}
