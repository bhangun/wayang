package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Standalone compatibility bypass for unstable project listing backend.
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 1)
public class ProjectsBypassFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        try {
            if (!"GET".equalsIgnoreCase(requestContext.getMethod())) {
                return;
            }

            String path = requestContext.getUriInfo().getPath();
            if (path == null) {
                return;
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            if ("api/v1/projects".equals(path)) {
                requestContext.abortWith(Response.ok("[]", MediaType.APPLICATION_JSON).build());
            }
        } catch (Throwable ignored) {
            requestContext.abortWith(Response.ok("[]", MediaType.APPLICATION_JSON).build());
        }
    }
}
