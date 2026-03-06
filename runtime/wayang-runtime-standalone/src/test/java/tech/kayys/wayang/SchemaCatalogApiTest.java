package tech.kayys.wayang;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class SchemaCatalogApiTest {

    @Test
    void shouldExposePlannerAndEvaluatorSchemasInCatalog() {
        given()
                .when()
                .get("/v1/schema/catalog")
                .then()
                .statusCode(200)
                .body("schemas.id", hasItem("agent-planner"))
                .body("schemas.id", hasItem("agent-evaluator"));
    }

    @Test
    void shouldExposeTypedPlannerAndEvaluatorFields() {
        given()
                .when()
                .get("/v1/schema/catalog/agent-planner")
                .then()
                .statusCode(200)
                .body("schema", containsString("\"goal\""))
                .body("schema", containsString("\"strategy\""));

        given()
                .when()
                .get("/v1/schema/catalog/agent-evaluator")
                .then()
                .statusCode(200)
                .body("schema", containsString("\"candidateOutput\""))
                .body("schema", containsString("\"criteria\""));
    }
}
