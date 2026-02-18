package tech.kayys.wayang;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class RuntimeResourceTest {

    @Test
    void testRuntimeStatusEndpoint() {
        given()
                .when().get("/api/runtime/status")
                .then()
                .statusCode(200)
                .body("ready", notNullValue())
                .body("components", notNullValue());
    }
}
