package com.example.data_quality_control;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.flyway.enabled=true"}
)
@Testcontainers
@AutoConfigureRestTestClient
class ApiSmokeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void smokeTest_allEndpointsAreAvailableAndReturnExpectedData() {
        // 1. GET /api/runs — должно быть 2 прогона из миграции V3
        JsonNode runs = restTestClient.get()
                .uri("/api/runs")
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();

        assertThat(runs).isNotNull();
        assertThat(runs.isArray()).isTrue();
        assertThat(runs.size()).isEqualTo(2);
        assertThat(runs.get(0).has("snapshotDate")).isTrue();
        assertThat(runs.get(0).has("issuesCount")).isTrue();

        // 2. GET /api/kkd/issues?run_id=1 — проверяем, что NULL-параметр больше не ломает SQL
        JsonNode issues = restTestClient.get()
                .uri("/api/kkd/issues?run_id=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();

        assertThat(issues).isNotNull();
        assertThat(issues.has("items")).isTrue();
        assertThat(issues.has("total")).isTrue();
        assertThat(issues.get("total").asInt()).isEqualTo(20); // В первом прогоне 20 проблем

        // 3. GET /api/kkd/summary?run_id=2&unit_id=1
        JsonNode summary = restTestClient.get()
                .uri("/api/kkd/summary?run_id=2&unit_id=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonNode.class)
                .returnResult()
                .getResponseBody();

        assertThat(summary).isNotNull();
        assertThat(summary.has("run")).isTrue();
        assertThat(summary.has("comparedTo")).isTrue();
        assertThat(summary.has("metrics")).isTrue();
        assertThat(summary.get("metrics").size()).isGreaterThanOrEqualTo(3); // Минимум 3 метрики
        assertThat(summary.has("issuesBySeverity")).isTrue();
        assertThat(summary.has("delta")).isTrue();

        // 4. POST /api/runs — запускаем новый прогон, ожидаем 201 Created
        restTestClient.post()
                .uri("/api/runs")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);
    }
}