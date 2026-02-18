package tech.kayys.wayang.control.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Event Stream API - For real-time canvas updates and system events.
 */
@Path("/api/v1/events")
@Produces(MediaType.SERVER_SENT_EVENTS)
@Tag(name = "Control Plane - Events", description = "Real-time event streaming")
public class EventStreamResource {
    // To be implemented with Redis/Kafka event bus
}
