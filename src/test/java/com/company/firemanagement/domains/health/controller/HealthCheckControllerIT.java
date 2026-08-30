package com.company.firemanagement.domains.health.controller;

import com.company.firemanagement.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckControllerIT extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnHealthyStatusAndVerifyDatabaseWrite() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/health", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<?, ?> body = response.getBody();
        assertThat(body.get("status")).isEqualTo("UP");
        assertThat(body.get("database")).isEqualTo("CONNECTED");

        Map<?, ?> verifiedAudit = (Map<?, ?>) body.get("verifiedAudit");
        assertThat(verifiedAudit).isNotNull();
        assertThat(verifiedAudit.get("recordId")).isNotNull();
        assertThat(verifiedAudit.get("status")).isEqualTo("OK");
        assertThat(verifiedAudit.get("createdBy")).isEqualTo("SYSTEM"); // Security fallback for unauthenticated auditing
        assertThat(verifiedAudit.get("createdAt")).isNotNull();
        assertThat(verifiedAudit.get("updatedAt")).isNotNull();
    }
}
