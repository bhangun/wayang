package tech.kayys.wayang.control.websocket;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.schema.catalog.BuiltinSchemaCatalog;

@ApplicationScoped
public class ControlPlaneSchemaRouter {

    public String resolveSchemaId(String channel, String schemaId) {
        String normalizedChannel = normalizeChannel(channel);
        String candidateSchema = schemaId == null || schemaId.isBlank()
                ? defaultSchemaForChannel(normalizedChannel)
                : schemaId;

        if (!isAllowedForChannel(normalizedChannel, candidateSchema)) {
            return null;
        }
        if (BuiltinSchemaCatalog.get(candidateSchema) == null) {
            return null;
        }
        return candidateSchema;
    }

    public Set<String> availableSchemas() {
        return BuiltinSchemaCatalog.ids();
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return "workflow";
        }
        return channel.trim().toLowerCase();
    }

    private String defaultSchemaForChannel(String channel) {
        return switch (channel) {
            case "wayang" -> BuiltinSchemaCatalog.WAYANG_SPEC;
            case "agent" -> BuiltinSchemaCatalog.AGENT_CONFIG;
            case "node" -> BuiltinSchemaCatalog.WORKFLOW_SPEC;
            default -> BuiltinSchemaCatalog.WORKFLOW_SPEC;
        };
    }

    private boolean isAllowedForChannel(String channel, String schemaId) {
        return switch (channel) {
            case "workflow" -> BuiltinSchemaCatalog.WORKFLOW.equals(schemaId)
                    || BuiltinSchemaCatalog.WORKFLOW_SPEC.equals(schemaId);
            case "wayang" -> BuiltinSchemaCatalog.WAYANG_SPEC.equals(schemaId);
            case "agent" -> BuiltinSchemaCatalog.AGENT_CONFIG.equals(schemaId)
                    || schemaId.startsWith("agent-");
            case "node" -> BuiltinSchemaCatalog.ids().contains(schemaId);
            default -> BuiltinSchemaCatalog.ids().contains(schemaId);
        };
    }
}
