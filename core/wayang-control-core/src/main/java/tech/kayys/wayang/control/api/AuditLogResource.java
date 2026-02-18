package tech.kayys.wayang.control.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Audit Log API - For tracking control plane activities.
 */
@Path("/api/v1/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Control Plane - Audit", description = "Audit logging and compliance")
public class AuditLogResource {
    // To be implemented with centralized audit service
}
