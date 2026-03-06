package tech.kayys.wayang;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

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
                .extract()
                .path("executionId");

        given()
                .when()
                .get("/api/v1/projects/{projectId}/executions/{executionId}", projectId, executionId)
                .then()
                .statusCode(200)
                .body("projectId", equalTo(projectId))
                .body("executionId", equalTo(executionId))
                .body("status", notNullValue());

        given()
                .when()
                .get("/api/v1/projects/{projectId}/executions/{executionId}/events", projectId, executionId)
                .then()
                .statusCode(200)
                .body("size()", notNullValue())
                .body("[0].type", equalTo("EXECUTION_STARTED"));
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
}
