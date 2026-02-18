package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/api-index")
@Produces(MediaType.APPLICATION_JSON)
public class ApiIndexResource {

    @GET
    public Map<String, Object> index() {
        return Map.of(
                "runtime", "wayang-runtime-standalone",
                "docs", Map.of(
                        "swaggerUi", "/q/swagger-ui",
                        "openApi", "/q/openapi"),
                "wayang", List.of(
                        "/api/runtime",
                        "/api/orchestration"),
                "gamelan", List.of(
                        "/api/v1/workflow-definitions",
                        "/api/v1/workflow-runs",
                        "/api/v1/executors",
                        "/api/v1/callbacks",
                        "/api/v1/plugins"),
                "gollek", List.of(
                        "/v1/infer",
                        "/v1/models",
                        "/v1/providers",
                        "/v1/converter/gguf"),
                "health", List.of(
                        "/health",
                        "/q/health",
                        "/q/health/live",
                        "/q/health/ready"));
    }
}
