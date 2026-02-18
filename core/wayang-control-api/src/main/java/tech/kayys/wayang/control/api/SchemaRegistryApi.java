package tech.kayys.wayang.control.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.control.service.SchemaRegistryService;
import tech.kayys.wayang.schema.validator.ValidationResult;

import java.util.Map;
import java.util.UUID;

@Path("/v1/schemas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SchemaRegistryApi {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaRegistryApi.class);

    @Inject
    SchemaRegistryService schemaRegistryService;

    @POST
    public Response registerSchema(@QueryParam("schemaId") String schemaId,
                                  @QueryParam("schemaType") String schemaType,
                                  String schema) {
        LOG.info("Registering schema: {}", schemaId);
        
        // Create metadata map (could be expanded to accept more metadata)
        Map<String, String> metadata = Map.of("registeredAt", String.valueOf(System.currentTimeMillis()));
        
        return schemaRegistryService.registerSchema(schemaId, schema, schemaType, metadata)
                .onItem().transform(v -> Response.status(Response.Status.CREATED).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error registering schema: " + schemaId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(Map.of("error", "Failed to register schema"))
                            .build();
                })
                .await().indefinitely();
    }

    @GET
    @Path("/{schemaId}")
    public Response getSchema(@PathParam("schemaId") String schemaId) {
        LOG.debug("Getting schema: {}", schemaId);
        
        return schemaRegistryService.getSchema(schemaId)
                .onItem().transform(schema -> {
                    if (schema != null) {
                        return Response.ok(schema).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error getting schema: " + schemaId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }

    @POST
    @Path("/{schemaId}/validate")
    public Response validateAgainstSchema(@PathParam("schemaId") String schemaId, Map<String, Object> data) {
        LOG.debug("Validating data against schema: {}", schemaId);
        
        return schemaRegistryService.validateAgainstSchema(schemaId, data)
                .onItem().transform(result -> Response.ok(result).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error validating against schema: " + schemaId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }

    @POST
    @Path("/validate")
    public Response validateSchema(String schema, Map<String, Object> data) {
        LOG.debug("Validating data against provided schema");
        
        return schemaRegistryService.validateSchema(schema, data)
                .onItem().transform(result -> Response.ok(result).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error validating schema", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }

    @DELETE
    @Path("/{schemaId}")
    public Response removeSchema(@PathParam("schemaId") String schemaId) {
        LOG.info("Removing schema: {}", schemaId);
        
        return schemaRegistryService.removeSchema(schemaId)
                .onItem().transform(v -> Response.noContent().build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error removing schema: " + schemaId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }
}