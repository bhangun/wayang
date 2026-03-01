package tech.kayys.wayang.schema.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.eip.schema.EIPSchema;
import tech.kayys.wayang.schema.catalog.BuiltinSchemaCatalog;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class SchemaApiDynamicGenerationTest {

    @Test
    public void testDynamicSplitterSchema() {
        // Fetch the Splitter schema from the catalog API
        given()
                .when().get("/v1/schema/catalog/" + EIPSchema.EIP_SPLITTER)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("schema", notNullValue())
                // Verify the schema string contains the DTO properties we defined in
                // SplitterDto
                .body("schema", containsString("\"expression\""))
                .body("schema", containsString("\"strategy\""))
                .body("schema", containsString("\"parallel\""))
                .body("schema", containsString("\"batchSize\"")); // We recently added this!
    }

    @Test
    public void testDynamicDeadLetterChannelSchema() {
        // Fetch the DeadLetterChannel schema
        given()
                .when().get("/v1/schema/catalog/" + BuiltinSchemaCatalog.EIP_DEAD_LETTER_CHANNEL)
                .then()
                .statusCode(200)
                .body("schema", containsString("\"channelName\""))
                .body("schema", containsString("\"retentionDays\"")); // Verified property mismatch fix
    }

    @Test
    public void testDynamicOrchestratorAgentSchema() {
        given()
                .when().get("/v1/schema/catalog/" + BuiltinSchemaCatalog.AGENT_ORCHESTRATOR)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("schema", notNullValue())
                .body("schema", containsString("\"orchestrationType\""))
                .body("schema", containsString("\"coordinationStrategy\""))
                .body("schema", containsString("\"agentTasks\""));
    }

    @Test
    public void testDynamicAnalyticAgentSchema() {
        given()
                .when().get("/v1/schema/catalog/" + BuiltinSchemaCatalog.AGENT_ANALYTIC)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("schema", notNullValue())
                // AnalyticAgentConfig fields from AgentConfig base
                .body("schema", containsString("\"instruction\""))
                .body("schema", containsString("\"preferredProvider\""));
    }

    @Test
    public void testDynamicHumanTaskSchema() {
        given()
                .when().get("/v1/schema/catalog/" + BuiltinSchemaCatalog.HITL_HUMAN_TASK)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("schema", notNullValue())
                // Core assignment fields
                .body("schema", containsString("\"assignTo\""))
                .body("schema", containsString("\"assigneeType\""))
                .body("schema", containsString("\"taskType\""))
                .body("schema", containsString("\"title\""))
                .body("schema", containsString("\"priority\""))
                // Due date fields
                .body("schema", containsString("\"dueInHours\""))
                .body("schema", containsString("\"dueInDays\""))
                // Nested configs
                .body("schema", containsString("\"escalationConfig\""))
                .body("schema", containsString("\"notificationConfig\""));
    }
}
