package tech.kayys.wayang.mcp.client.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class WayangMcpClientResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/wayang-mcp-client")
                .then()
                .statusCode(200)
                .body(is("Hello wayang-mcp-client"));
    }
}
