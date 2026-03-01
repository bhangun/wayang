package tech.kayys.wayang.runtime.standalone.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tech.kayys.wayang.schema.catalog.BuiltinSchemaCatalog;

@Path("/api/v1/schema")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SchemaCatalogResource {

    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @GET
    @Path("/catalog")
    public Map<String, Object> catalog() {
        List<Map<String, String>> schemas = BuiltinSchemaCatalog.ids().stream()
                .map(id -> Map.of(
                        "id", id,
                        "path", "/api/v1/schema/catalog/" + id,
                        "validatePath", "/api/v1/schema/catalog/" + id + "/validate"))
                .toList();
        return Map.of("schemas", schemas);
    }

    @GET
    @Path("/catalog/{schemaId}")
    public Response schema(@PathParam("schemaId") String schemaId) {
        String schemaJson = BuiltinSchemaCatalog.get(schemaId);
        if (schemaJson == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                            "message", "Schema not found: " + schemaId,
                            "availableSchemas", BuiltinSchemaCatalog.ids()))
                    .build();
        }
        return Response.ok(Map.of("id", schemaId, "schema", schemaJson)).build();
    }

    @POST
    @Path("/catalog/{schemaId}/validate")
    public Response validate(
            @PathParam("schemaId") String schemaId,
            Map<String, Object> payload) {
        String schemaJson = BuiltinSchemaCatalog.get(schemaId);
        if (schemaJson == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                            "message", "Schema not found: " + schemaId,
                            "availableSchemas", BuiltinSchemaCatalog.ids()))
                    .build();
        }

        try {
            JsonNode schemaNode = OBJECT_MAPPER.readTree(schemaJson);
            JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaNode);
            JsonNode dataNode = OBJECT_MAPPER.valueToTree(payload);
            Set<ValidationMessage> errors = schema.validate(dataNode);
            List<String> messages = errors.stream()
                    .map(ValidationMessage::getMessage)
                    .toList();
            return Response.ok(Map.of(
                    "valid", errors.isEmpty(),
                    "message", errors.isEmpty() ? "valid" : String.join(", ", messages),
                    "errors", messages)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                            "valid", false,
                            "message", "Validation error: " + e.getMessage()))
                    .build();
        }
    }
}
