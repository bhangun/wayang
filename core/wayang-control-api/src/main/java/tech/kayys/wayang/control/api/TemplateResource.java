package tech.kayys.wayang.control.api;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.control.dto.CreateTemplateRequest;
import tech.kayys.wayang.schema.DefinitionType;
import tech.kayys.wayang.schema.WayangSpec;

import java.util.UUID;

/**
 * REST API for Workflow Template management.
 */
@Path("/api/v1/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TemplateResource {

        @Inject
        WayangDefinitionService definitionService;

        @POST
        public Uni<Response> createTemplate(@QueryParam("projectId") UUID projectId,
                        @Valid CreateTemplateRequest request) {

                WayangSpec spec = new WayangSpec();
                // Map legacy canvas definition to spec if present
                // (In a real scenario, this would be more complex)

                return definitionService.create("default", projectId, request.templateName(),
                                request.description(), DefinitionType.WORKFLOW_TEMPLATE, spec, "system")
                                .map(template -> Response.status(Response.Status.CREATED).entity(template).build());
        }

        @GET
        @Path("/{templateId}")
        public Uni<Response> getTemplate(@PathParam("templateId") UUID templateId) {
                return definitionService.findById(templateId)
                                .map(template -> template != null ? Response.ok(template).build()
                                                : Response.status(Response.Status.NOT_FOUND).build());
        }

        @POST
        @Path("/{templateId}/publish")
        public Uni<Response> publishTemplate(@PathParam("templateId") UUID templateId) {
                return definitionService.publish(templateId, "system")
                                .map(def -> Response
                                                .ok(java.util.Map.of("workflowDefinitionId", def.workflowDefinitionId))
                                                .build());
        }
}
