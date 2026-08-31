package com.company.firemanagement.domains.user.service;

import com.company.firemanagement.common.exception.BaseException;
import com.company.firemanagement.common.exception.ErrorCode;
import com.company.firemanagement.domains.geography.entity.FireStation;
import com.company.firemanagement.domains.geography.repository.FireStationRepository;
import com.company.firemanagement.domains.user.dto.LoginRequest;
import com.company.firemanagement.domains.user.dto.LoginResponse;
import com.company.firemanagement.domains.user.dto.RegisterRequest;
import com.company.firemanagement.domains.user.dto.RegisterResponse;
import com.company.firemanagement.domains.user.entity.EmployeeProfile;
import com.company.firemanagement.domains.user.entity.Role;
import com.company.firemanagement.domains.user.entity.User;
import com.company.firemanagement.domains.user.repository.EmployeeProfileRepository;
import com.company.firemanagement.domains.user.repository.RoleRepository;
import com.company.firemanagement.domains.user.repository.UserRepository;
import com.company.firemanagement.security.jwt.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final FireStationRepository fireStationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.security.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @PostConstruct
    @Transactional
    public void initRoles() {
        log.info("Initializing system roles...");
        ensureRole("ROLE_CITIZEN", "Default role for public citizens");
        ensureRole("ROLE_FIREFIGHTER", "Role for field firefighters");
        ensureRole("ROLE_ADMIN", "Role for fire station administrators");
    }

    private void ensureRole(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }

    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        log.info("Registering user with username: {} and email: {}", request.getUsername(), request.getEmail());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BaseException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Username is already taken");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BaseException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Email is already registered");
        }

        // Map roles
        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoleNames()) {
            String formattedRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
            Role role = roleRepository.findByName(formattedRole)
                    .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Role not found: " + roleName));
            roles.add(role);
        }

        // Check employee-specific constraints
        boolean isEmployee = request.getRoleNames().stream()
                .anyMatch(r -> r.equalsIgnoreCase("ROLE_FIREFIGHTER") || r.equalsIgnoreCase("FIREFIGHTER")
                        || r.equalsIgnoreCase("ROLE_ADMIN") || r.equalsIgnoreCase("ADMIN"));

        FireStation station = null;
        if (isEmployee) {
            if (request.getStationId() == null) {
                throw new BaseException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Station ID is required for employee registration");
            }
            if (request.getEmployeeCode() == null || request.getEmployeeCode().isBlank()) {
                throw new BaseException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Employee code is required for employee registration");
            }
            station = fireStationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Fire station not found with ID: " + request.getStationId()));

            if (employeeProfileRepository.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
                throw new BaseException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Employee code is already in use");
            }
        }

        // Create User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIsActive(true);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Save Employee Profile if applicable
        if (isEmployee) {
            EmployeeProfile profile = new EmployeeProfile();
            profile.setUser(savedUser);
            profile.setStation(station);
            profile.setEmployeeCode(request.getEmployeeCode());
            employeeProfileRepository.save(profile);
        }

        return RegisterResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse loginUser(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BaseException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        if (!user.getIsActive()) {
            throw new BaseException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "User account is suspended");
        }

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Generate JWT token. The claims list we send to jwtTokenProvider should omit "ROLE_" prefix if it auto-prefixes,
        // but wait! In JwtTokenProvider:
        // authorities = roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
        // If we save roles as "ROLE_FIREFIGHTER" in database, we should strip "ROLE_" before encoding into JWT so that when it is parsed,
        // it doesn't get double prefixed!
        // Let's strip "ROLE_" from the role names we put in the JWT token:
        List<String> tokenRoles = roleNames.stream()
                .map(name -> name.startsWith("ROLE_") ? name.substring(5) : name)
                .collect(Collectors.toList());

        String token = jwtTokenProvider.generateToken(user.getId().toString(), user.getEmail(), tokenRoles);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .roles(roleNames)
                .build();
    }
}
