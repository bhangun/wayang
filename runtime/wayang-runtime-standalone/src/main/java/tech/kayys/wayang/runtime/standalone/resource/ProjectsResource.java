package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * Standalone-safe projects endpoint.
 * Returns an empty list when control-plane persistence is unavailable.
 */
@Path("/api/v1/projects")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
public class ProjectsResource {

    @GET
    public List<Map<String, Object>> listProjects() {
        return List.of();
    }
}
