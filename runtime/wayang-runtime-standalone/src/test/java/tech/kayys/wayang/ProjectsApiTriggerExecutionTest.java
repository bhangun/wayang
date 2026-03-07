package tech.kayys.wayang;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ProjectsApiTriggerExecutionTest {

    private static final String REQUEST_ID = "req-trigger-e2e-001";

    @Test
    void shouldExecuteTriggerSpecAndExposeExecutionLifecycle() {
        String projectId = given()
                .contentType("application/json")
                .body(Map.of(
                        "projectName", "Trigger API Test Project",
                        "description", "verify trigger execution path"))
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(201)
                .body("projectId", notNullValue())
                .extract()
                .path("projectId");

        String executionId = given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .header("X-Request-Id", REQUEST_ID)
                .body(Map.of(
                        "name", "trigger-api-run",
                        "spec", Map.of(
                                "specVersion", "1.0.0",
                                "canvas", Map.of(
                                        "nodes", java.util.List.of(Map.of(
                                                "id", "trigger-node-1",
                                                "type", "trigger-schedule",
                                                "label", "Schedule Trigger",
                                                "config", Map.of(
                                                        "mode", "interval",
                                                        "intervalSeconds", 30,
                                                        "timezone", "Asia/Jakarta"))),
                                        "edges", java.util.List.of()))))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(202)
                .body("projectId", equalTo(projectId))
                .body("executionId", notNullValue())
                .body("requestId", equalTo(REQUEST_ID))
                .body("queuedAt", notNullValue())
                .body("startedAt", notNullValue())
                .body("queueDurationMs", notNullValue())
                .extract()
                .path("executionId");

        Response statusResponse = given()
                .when()
                .get("/api/v1/projects/{projectId}/executions/{executionId}", projectId, executionId)
                .then()
                .statusCode(200)
                .body("projectId", equalTo(projectId))
                .body("executionId", equalTo(executionId))
                .body("status", notNullValue())
                .extract()
                .response();

        String etag = statusResponse.getHeader("ETag");
        org.junit.jupiter.api.Assertions.assertNotNull(etag, "status response should include ETag");

        given()
                .header("If-None-Match", etag)
                .when()
                .get("/api/v1/projects/{projectId}/executions/{executionId}", projectId, executionId)
                .then()
                .statusCode(304);

        given()
                .when()
                .get("/api/v1/projects/{projectId}/executions/{executionId}/events", projectId, executionId)
                .then()
                .statusCode(200)
                .body("size()", notNullValue())
                .body("[0].type", equalTo("EXECUTION_QUEUED"))
                .body("[1].type", equalTo("EXECUTION_STARTED"))
                .body("[0].metadata.requestId", equalTo(REQUEST_ID))
                .body("[1].metadata.requestId", equalTo(REQUEST_ID));
    }

    @Test
    void shouldAcceptAgentExecutionSpecWithLocalCloudProviderAndVaultRefs() {
        String projectId = given()
                .contentType("application/json")
                .body(Map.of(
                        "projectName", "Agent Provider/Vault Coverage",
                        "description", "verify provider mode and vault refs are accepted by executions API"))
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(201)
                .body("projectId", notNullValue())
                .extract()
                .path("projectId");

        Map<String, Object> agentNode = new java.util.LinkedHashMap<>();
        agentNode.put("id", "agent-1");
        agentNode.put("type", "agent-basic");
        agentNode.put("label", "Agent Node");
        agentNode.put("config", Map.of(
                "providerMode", "cloud",
                "cloudProvider", Map.of(
                        "providerId", "tech.kayys/gemini-provider",
                        "model", "gemini-2.0-flash",
                        "region", "us-central1"),
                "localProvider", Map.of(
                        "providerId", "tech.kayys/ollama-provider",
                        "model", "llama3.2"),
                "credentialRefs", java.util.List.of(
                        Map.of(
                                "name", "gemini-api-key",
                                "backend", "vault",
                                "path", "wayang/providers/gemini",
                                "key", "apiKey"))));

        Map<String, Object> spec = new java.util.LinkedHashMap<>();
        spec.put("specVersion", "1.0.0");
        spec.put("canvas", Map.of(
                "nodes", java.util.List.of(agentNode),
                "edges", java.util.List.of()));
        spec.put("extensions", Map.of(
                "vault", Map.of(
                        "backend", "vault",
                        "tenantId", "community",
                        "pathPrefix", "wayang/providers")));

        String executionId = given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .body(Map.of(
                        "name", "agent-provider-vault-run",
                        "spec", spec))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(202)
                .body("projectId", equalTo(projectId))
                .body("executionId", notNullValue())
                .body("status", equalTo("STARTED"))
                .extract()
                .path("executionId");

        given()
                .when()
                .get("/api/v1/projects/{projectId}/executions/{executionId}/events", projectId, executionId)
                .then()
                .statusCode(200)
                .body("[0].metadata.agentConfigCoverage.agentConfigNodes", greaterThanOrEqualTo(1))
                .body("[0].metadata.agentConfigCoverage.cloudProviderConfigs", equalTo(1))
                .body("[0].metadata.agentConfigCoverage.localProviderConfigs", equalTo(1))
                .body("[0].metadata.agentConfigCoverage.credentialRefs", equalTo(1))
                .body("[0].metadata.agentConfigCoverage.vaultConfigs", equalTo(1))
                .body("[0].metadata.agentConfigCoverage.secretResolutionChecked", notNullValue());
    }

    @Test
    void shouldApplyGlobalVaultPathPrefixToCredentialRefs() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "tenantId", "community",
                        "path", "wayang/providers/gemini",
                        "data", Map.of("apiKey", "secret-value")))
                .when()
                .post("/api/v1/secrets")
                .then()
                .statusCode(201);

        String projectId = given()
                .contentType("application/json")
                .body(Map.of(
                        "projectName", "Agent Secret Resolution Coverage",
                        "description", "verify credential refs are resolved into execution metadata"))
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(201)
                .body("projectId", notNullValue())
                .extract()
                .path("projectId");

        Map<String, Object> agentNode = new java.util.LinkedHashMap<>();
        agentNode.put("id", "agent-1");
        agentNode.put("type", "agent-basic");
        agentNode.put("label", "Agent Node");
        agentNode.put("config", Map.of(
                "providerMode", "cloud",
                "cloudProvider", Map.of(
                        "providerId", "tech.kayys/gemini-provider",
                        "model", "gemini-2.0-flash"),
                "credentialRefs", java.util.List.of(
                        Map.of(
                                "name", "gemini-api-key",
                                "backend", "vault",
                                "path", "gemini",
                                "key", "apiKey"))));

        Map<String, Object> spec = new java.util.LinkedHashMap<>();
        spec.put("specVersion", "1.0.0");
        spec.put("canvas", Map.of(
                "nodes", java.util.List.of(agentNode),
                "edges", java.util.List.of()));
        spec.put("extensions", Map.of(
                "vault", Map.of(
                        "backend", "vault",
                        "tenantId", "community",
                        "pathPrefix", "wayang/providers")));

        String executionId = given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .body(Map.of(
                        "name", "agent-secret-resolution-run",
                        "spec", spec))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(202)
                .body("projectId", equalTo(projectId))
                .body("executionId", notNullValue())
                .extract()
                .path("executionId");

        List<Map<String, Object>> events = given()
                .when()
                .get("/api/v1/projects/{projectId}/executions/{executionId}/events", projectId, executionId)
                .then()
                .statusCode(200)
                .extract()
                .as(List.class);

        Map<String, Object> firstEvent = events.get(0);
        Map<String, Object> metadata = (Map<String, Object>) firstEvent.get("metadata");
        Map<String, Object> coverage = (Map<String, Object>) metadata.get("agentConfigCoverage");
        int credentialRefs = ((Number) coverage.get("credentialRefs")).intValue();
        int resolved = ((Number) coverage.get("credentialRefsResolved")).intValue();
        int missing = ((Number) coverage.get("credentialRefsMissing")).intValue();

        assertEquals(1, credentialRefs, "expected one credential ref in coverage");
        assertEquals(1, resolved + missing, "expected exactly one credential ref outcome");

        if (missing > 0) {
            List<String> missingPaths = (List<String>) coverage.get("missingSecretPaths");
            assertTrue(missingPaths.contains("wayang/providers/gemini"),
                    "missing secret path should include normalized prefix path");
        }
        if (resolved > 0) {
            List<String> resolvedNames = (List<String>) metadata.get("resolvedCredentialNames");
            assertTrue(resolvedNames.contains("gemini-api-key"),
                    "resolved credential names should include gemini-api-key");
        }
    }

    @Test
    void shouldSupportDryRunWithoutStartingExecution() {
        String projectId = given()
                .contentType("application/json")
                .body(Map.of(
                        "projectName", "Dry Run API Test Project",
                        "description", "validate only flow"))
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(201)
                .body("projectId", notNullValue())
                .extract()
                .path("projectId");

        given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .body(Map.of(
                        "name", "dry-run-api",
                        "dryRun", true,
                        "spec", Map.of(
                                "specVersion", "1.0.0",
                                "canvas", Map.of(
                                        "nodes", java.util.List.of(),
                                        "edges", java.util.List.of()))))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(200)
                .body("projectId", equalTo(projectId))
                .body("dryRun", equalTo(true))
                .body("validated", equalTo(true))
                .body("canExecute", equalTo(true))
                .body("status", equalTo("DRY_RUN_VALID"));

        given()
                .when()
                .get("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void shouldReplayExistingExecutionForSameIdempotencyKey() {
        String projectId = given()
                .contentType("application/json")
                .body(Map.of(
                        "projectName", "Idempotency API Project",
                        "description", "duplicate submit protection"))
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(201)
                .body("projectId", notNullValue())
                .extract()
                .path("projectId");

        String executionId = given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .header("Idempotency-Key", "idem-api-1")
                .body(Map.of(
                        "name", "idempotent-run",
                        "spec", Map.of(
                                "specVersion", "1.0.0",
                                "canvas", Map.of(
                                        "nodes", java.util.List.of(Map.of(
                                                "id", "trigger-node-idem-1",
                                                "type", "trigger-schedule",
                                                "label", "Schedule Trigger",
                                                "config", Map.of(
                                                        "mode", "interval",
                                                        "intervalSeconds", 30,
                                                        "timezone", "Asia/Jakarta"))),
                                        "edges", java.util.List.of()))))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(202)
                .body("executionId", notNullValue())
                .extract()
                .path("executionId");

        given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .header("Idempotency-Key", "idem-api-1")
                .body(Map.of(
                        "name", "idempotent-run-retry",
                        "spec", Map.of(
                                "specVersion", "1.0.0",
                                "canvas", Map.of(
                                        "nodes", java.util.List.of(Map.of(
                                                "id", "trigger-node-idem-1",
                                                "type", "trigger-schedule",
                                                "label", "Schedule Trigger",
                                                "config", Map.of(
                                                        "mode", "interval",
                                                        "intervalSeconds", 30,
                                                        "timezone", "Asia/Jakarta"))),
                                        "edges", java.util.List.of()))))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(200)
                .body("executionId", equalTo(executionId))
                .body("idempotentReplay", equalTo(true));

        given()
                .when()
                .get("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }

    @Test
    void shouldCreateNewExecutionWhenReplayWindowIsDisabled() {
        String projectId = given()
                .contentType("application/json")
                .body(Map.of(
                        "projectName", "Idempotency Replay Window API Project",
                        "description", "replay window disabled should not deduplicate"))
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(201)
                .body("projectId", notNullValue())
                .extract()
                .path("projectId");

        String firstExecutionId = given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .header("Idempotency-Key", "idem-api-window-1")
                .body(Map.of(
                        "name", "idempotent-window-run-1",
                        "idempotencyReplayWindowSeconds", 0,
                        "spec", Map.of(
                                "specVersion", "1.0.0",
                                "canvas", Map.of(
                                        "nodes", java.util.List.of(Map.of(
                                                "id", "trigger-node-idem-window-1",
                                                "type", "trigger-schedule",
                                                "label", "Schedule Trigger",
                                                "config", Map.of(
                                                        "mode", "interval",
                                                        "intervalSeconds", 30,
                                                        "timezone", "Asia/Jakarta"))),
                                        "edges", java.util.List.of()))))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(202)
                .body("executionId", notNullValue())
                .extract()
                .path("executionId");

        String secondExecutionId = given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .header("Idempotency-Key", "idem-api-window-1")
                .body(Map.of(
                        "name", "idempotent-window-run-2",
                        "idempotencyReplayWindowSeconds", 0,
                        "spec", Map.of(
                                "specVersion", "1.0.0",
                                "canvas", Map.of(
                                        "nodes", java.util.List.of(Map.of(
                                                "id", "trigger-node-idem-window-2",
                                                "type", "trigger-schedule",
                                                "label", "Schedule Trigger",
                                                "config", Map.of(
                                                        "mode", "interval",
                                                        "intervalSeconds", 30,
                                                        "timezone", "Asia/Jakarta"))),
                                        "edges", java.util.List.of()))))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(202)
                .body("executionId", notNullValue())
                .extract()
                .path("executionId");

        org.junit.jupiter.api.Assertions.assertTrue(!firstExecutionId.equals(secondExecutionId),
                "disabled replay window should produce distinct executions");
    }

    @Test
    void shouldRejectInvalidStopAndResumeTransitions() {
        String projectId = given()
                .contentType("application/json")
                .body(Map.of(
                        "projectName", "Lifecycle Transition API Project",
                        "description", "strict transition checks"))
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(201)
                .body("projectId", notNullValue())
                .extract()
                .path("projectId");

        String executionId = given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .body(Map.of(
                        "name", "lifecycle-run",
                        "spec", Map.of(
                                "specVersion", "1.0.0",
                                "canvas", Map.of(
                                        "nodes", java.util.List.of(Map.of(
                                                "id", "trigger-node-lifecycle-1",
                                                "type", "trigger-schedule",
                                                "label", "Schedule Trigger",
                                                "config", Map.of(
                                                        "mode", "interval",
                                                        "intervalSeconds", 30,
                                                        "timezone", "Asia/Jakarta"))),
                                        "edges", java.util.List.of()))))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(202)
                .body("executionId", notNullValue())
                .extract()
                .path("executionId");

        given()
                .contentType("application/json")
                .body(Map.of())
                .when()
                .post("/api/v1/projects/{projectId}/executions/{executionId}/stop", projectId, executionId)
                .then()
                .statusCode(200);

        given()
                .contentType("application/json")
                .body(Map.of())
                .when()
                .post("/api/v1/projects/{projectId}/executions/{executionId}/stop", projectId, executionId)
                .then()
                .statusCode(409)
                .body("errorCode", equalTo("EXECUTION_INVALID_TRANSITION"))
                .body("details.fromStatus", equalTo("STOPPED"))
                .body("details.toStatus", equalTo("STOPPED"));

        given()
                .contentType("application/json")
                .body(Map.of())
                .when()
                .post("/api/v1/projects/{projectId}/executions/{executionId}/resume", projectId, executionId)
                .then()
                .statusCode(409)
                .body("errorCode", equalTo("EXECUTION_INVALID_TRANSITION"))
                .body("details.fromStatus", equalTo("STOPPED"))
                .body("details.toStatus", equalTo("RUNNING"));
    }

    @Test
    void shouldRejectUnsupportedStopReason() {
        String projectId = given()
                .contentType("application/json")
                .body(Map.of(
                        "projectName", "Stop Reason Validation Project",
                        "description", "stop reason taxonomy validation"))
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(201)
                .body("projectId", notNullValue())
                .extract()
                .path("projectId");

        String executionId = given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .body(Map.of(
                        "name", "stop-reason-run",
                        "spec", Map.of(
                                "specVersion", "1.0.0",
                                "canvas", Map.of(
                                        "nodes", java.util.List.of(Map.of(
                                                "id", "trigger-node-stop-reason-1",
                                                "type", "trigger-schedule",
                                                "label", "Schedule Trigger",
                                                "config", Map.of(
                                                        "mode", "interval",
                                                        "intervalSeconds", 30,
                                                        "timezone", "Asia/Jakarta"))),
                                        "edges", java.util.List.of()))))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(202)
                .body("executionId", notNullValue())
                .extract()
                .path("executionId");

        given()
                .contentType("application/json")
                .body(Map.of("reason", "BOGUS_REASON"))
                .when()
                .post("/api/v1/projects/{projectId}/executions/{executionId}/stop", projectId, executionId)
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("INVALID_STOP_REASON"))
                .body("details.providedReason", equalTo("BOGUS_REASON"))
                .body("details.supportedReasons", hasItem("USER_REQUEST"));
    }

    @Test
    void shouldRejectStopWithIfMatchVersionConflict() {
        String projectId = given()
                .contentType("application/json")
                .body(Map.of(
                        "projectName", "If-Match Version Conflict Project",
                        "description", "optimistic concurrency validation"))
                .when()
                .post("/api/v1/projects")
                .then()
                .statusCode(201)
                .body("projectId", notNullValue())
                .extract()
                .path("projectId");

        String executionId = given()
                .contentType("application/json")
                .header("X-Tenant-Id", "community")
                .body(Map.of(
                        "name", "version-conflict-run",
                        "spec", Map.of(
                                "specVersion", "1.0.0",
                                "canvas", Map.of(
                                        "nodes", java.util.List.of(Map.of(
                                                "id", "trigger-node-version-1",
                                                "type", "trigger-schedule",
                                                "label", "Schedule Trigger",
                                                "config", Map.of(
                                                        "mode", "interval",
                                                        "intervalSeconds", 30,
                                                        "timezone", "Asia/Jakarta"))),
                                        "edges", java.util.List.of()))))
                .when()
                .post("/api/v1/projects/{projectId}/executions", projectId)
                .then()
                .statusCode(202)
                .body("executionId", notNullValue())
                .extract()
                .path("executionId");

        Response conflict = given()
                .contentType("application/json")
                .header("If-Match", "\"99\"")
                .body(Map.of("reason", "USER_REQUEST"))
                .when()
                .post("/api/v1/projects/{projectId}/executions/{executionId}/stop", projectId, executionId)
                .then()
                .statusCode(409)
                .body("errorCode", equalTo("EXECUTION_VERSION_CONFLICT"))
                .body("details.executionId", equalTo(executionId))
                .body("details.expectedVersion", equalTo(99))
                .body("details.currentVersion", notNullValue())
                .body("retryable", equalTo(true))
                .body("retryAfterSeconds", notNullValue())
                .extract()
                .response();
        org.junit.jupiter.api.Assertions.assertNotNull(conflict.getHeader("Retry-After"));
    }

    @Test
    void shouldReturnRateLimitHeadersAnd429WhenSubmitLimitExceeded() {
        String originalLimit = System.getProperty("wayang.runtime.standalone.execution.rate-limit.per-minute");
        String originalEnabled = System.getProperty("wayang.runtime.standalone.execution.rate-limit.enabled");
        System.setProperty("wayang.runtime.standalone.execution.rate-limit.enabled", "true");
        System.setProperty("wayang.runtime.standalone.execution.rate-limit.per-minute", "1");
        try {
            String projectId = given()
                    .contentType("application/json")
                    .body(Map.of(
                            "projectName", "Rate Limit API Project",
                            "description", "submit rate limiting"))
                    .when()
                    .post("/api/v1/projects")
                    .then()
                    .statusCode(201)
                    .body("projectId", notNullValue())
                    .extract()
                    .path("projectId");

            String tenant = "tenant-rate-limit-it-" + java.util.UUID.randomUUID();
            Response first = given()
                    .contentType("application/json")
                    .header("X-Tenant-Id", tenant)
                    .body(Map.of(
                            "name", "rate-limit-it-1",
                            "spec", Map.of(
                                    "specVersion", "1.0.0",
                                    "canvas", Map.of(
                                            "nodes", java.util.List.of(Map.of(
                                                    "id", "trigger-node-rate-limit-1",
                                                    "type", "trigger-schedule",
                                                    "label", "Schedule Trigger",
                                                    "config", Map.of(
                                                            "mode", "interval",
                                                            "intervalSeconds", 30,
                                                            "timezone", "Asia/Jakarta"))),
                                            "edges", java.util.List.of()))))
                    .when()
                    .post("/api/v1/projects/{projectId}/executions", projectId)
                    .then()
                    .statusCode(202)
                    .extract()
                    .response();
            org.junit.jupiter.api.Assertions.assertNotNull(first.getHeader("X-RateLimit-Limit"));
            org.junit.jupiter.api.Assertions.assertNotNull(first.getHeader("X-RateLimit-Remaining"));
            org.junit.jupiter.api.Assertions.assertNotNull(first.getHeader("X-RateLimit-Reset"));

            given()
                    .contentType("application/json")
                    .header("X-Tenant-Id", tenant)
                    .body(Map.of(
                            "name", "rate-limit-it-2",
                            "spec", Map.of(
                                    "specVersion", "1.0.0",
                                    "canvas", Map.of(
                                            "nodes", java.util.List.of(Map.of(
                                                    "id", "trigger-node-rate-limit-2",
                                                    "type", "trigger-schedule",
                                                    "label", "Schedule Trigger",
                                                    "config", Map.of(
                                                            "mode", "interval",
                                                            "intervalSeconds", 30,
                                                            "timezone", "Asia/Jakarta"))),
                                            "edges", java.util.List.of()))))
                    .when()
                    .post("/api/v1/projects/{projectId}/executions", projectId)
                    .then()
                    .statusCode(429)
                    .body("errorCode", equalTo("EXECUTION_RATE_LIMITED"))
                    .body("retryable", equalTo(true))
                    .body("retryAfterSeconds", notNullValue());
        } finally {
            if (originalEnabled == null) {
                System.clearProperty("wayang.runtime.standalone.execution.rate-limit.enabled");
            } else {
                System.setProperty("wayang.runtime.standalone.execution.rate-limit.enabled", originalEnabled);
            }
            if (originalLimit == null) {
                System.clearProperty("wayang.runtime.standalone.execution.rate-limit.per-minute");
            } else {
                System.setProperty("wayang.runtime.standalone.execution.rate-limit.per-minute", originalLimit);
            }
        }
    }
}
