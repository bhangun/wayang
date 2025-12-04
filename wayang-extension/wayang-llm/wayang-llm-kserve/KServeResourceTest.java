package tech.kayys.wayang.models.api.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.models.kserve.dto.InferenceRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class KServeResourceTest {
    
    @Test
    void testServerLive() {
        given()
            .when()
            .get("/v2/health/live")
            .then()
            .statusCode(200)
            .body("ready", is(true));
    }
    
    @Test
    void testServerReady() {
        given()
            .when()
            .get("/v2/health/ready")
            .then()
            .statusCode(200)
            .body("ready", is(true));
    }
    
    @Test
    void testServerMetadata() {
        given()
            .when()
            .get("/v2")
            .then()
            .statusCode(200)
            .body("name", is("wayang-models"))
            .body("version", notNullValue())
            .body("extensions", notNullValue());
    }
    
    @Test
    void testModelInference() {
        InferenceRequest request = InferenceRequest.builder()
            .id(UUID.randomUUID().toString())
            .inputs(List.of(
                InferenceRequest.InferInputTensor.builder()
                    .name("prompt")
                    .datatype("BYTES")
                    .shape(List.of(1L))
                    .data(List.of("Hello, how are you?"))
                    .build()
            ))
            .parameters(Map.of(
                "max_tokens", 100,
                "temperature", 0.7,
                "tenant_id", "test-tenant"
            ))
            .build();
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/v2/models/llama3-8b/infer")
            .then()
            .statusCode(200)
            .body("model_name", is("llama3-8b"))
            .body("outputs", notNullValue())
            .body("outputs[0].name", is("text_output"))
            .body("outputs[0].datatype", is("BYTES"));
    }
    
    @Test
    void testModelMetadata() {
        given()
            .when()
            .get("/v2/models/llama3-8b")
            .then()
            .statusCode(200)
            .body("name", notNullValue())
            .body("platform", notNullValue())
            .body("inputs", notNullValue())
            .body("outputs", notNullValue());
    }
    
    @Test
    void testModelReady() {
        given()
            .when()
            .get("/v2/models/llama3-8b/ready")
            .then()
            .statusCode(200)
            .body("name", is("llama3-8b"))
            .body("ready", notNullValue());
    }
}