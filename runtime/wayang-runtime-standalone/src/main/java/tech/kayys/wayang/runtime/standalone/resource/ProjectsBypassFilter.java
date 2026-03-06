package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

/**
 * Legacy compatibility filter. Kept as no-op.
 * Project endpoints are now handled by {@link ProjectsResource}.
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 1)
public class ProjectsBypassFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // no-op
    }
}
