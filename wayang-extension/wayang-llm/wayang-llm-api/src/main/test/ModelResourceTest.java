package tech.kayys.wayang.models.api.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.models.api.dto.ModelRequest;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class ModelResourceTest {
    
    @Test
    void testInference() {
        ModelRequest request = ModelRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .tenantId("test-tenant")
            .type("chat")
            .prompt("Hello, how are you?")
            .maxTokens(100)
            .temperature(0.7)
            .build();
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/models/infer")
            .then()
            .statusCode(200)
            .body("requestId", is(request.getRequestId()))
            .body("status", is("ok"))
            .body("content", notNullValue());
    }
    
    @Test
    void testHealth() {
        given()
            .when()
            .get("/api/v1/models/health")
            .then()
            .statusCode(200)
            .body("healthy", is(true));
    }
}