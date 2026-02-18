package tech.kayys.wayang.control.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Monitoring API - For control plane health and performance metrics.
 */
@Path("/api/v1/monitoring")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Control Plane - Monitoring", description = "System health and telemetry")
public class MonitoringResource {
    // Integration with Prometheus/Micrometer
}
