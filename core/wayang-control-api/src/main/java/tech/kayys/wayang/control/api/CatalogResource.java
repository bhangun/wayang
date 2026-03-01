package tech.kayys.wayang.control.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Unified Catalog API - For searching across nodes, templates, and patterns.
 */
@Path("/api/v1/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Control Plane - Catalog", description = "Unified resource discovery")
public class CatalogResource {
    // To be implemented as an aggregator service
}
