package com.company.firemanagement.domains.user.controller;

import com.company.firemanagement.domains.user.dto.LoginRequest;
import com.company.firemanagement.domains.user.dto.LoginResponse;
import com.company.firemanagement.domains.user.dto.RegisterRequest;
import com.company.firemanagement.domains.user.dto.RegisterResponse;
import com.company.firemanagement.domains.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register user: {}", request.getUsername());
        RegisterResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to authenticate user: {}", request.getUsername());
        LoginResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }
}
