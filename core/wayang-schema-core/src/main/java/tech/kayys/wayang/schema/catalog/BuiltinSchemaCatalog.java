package tech.kayys.wayang.schema.catalog;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import tech.kayys.wayang.plugin.spi.node.NodeDefinition;
import tech.kayys.wayang.plugin.spi.node.NodeProvider;

/**
 * Unified JSON Schema catalog.
 *
 * Core schemas (workflow, plugin-config) are registered inline.
 * Node schemas are discovered at runtime via {@link NodeProvider}.
 * Standalone schemas are discovered via {@link SchemaProvider}.
 */
public final class BuiltinSchemaCatalog {

  public static final String WORKFLOW = "workflow";
  public static final String PLUGIN_CONFIG = "plugin-config";

  // Vector Executor
  public static final String VECTOR_SEARCH = "vector-search-node";
  public static final String VECTOR_UPSERT = "vector-upsert-node";

  // Agent
  public static final String AGENT_CONFIG = "agent-config";

  private static final String WORKFLOW_SCHEMA = """
      {
        "type": "object",
        "properties": {
          "name": { "type": "string" },
          "description": { "type": "string" },
          "nodes": {
            "type": "array",
            "items": { "$ref": "#/definitions/node" }
          },
          "connections": {
            "type": "array",
            "items": { "$ref": "#/definitions/connection" }
          }
        },
        "required": ["name", "nodes"],
        "definitions": {
          "node": {
            "type": "object",
            "properties": {
              "id": { "type": "string" },
              "type": { "type": "string" },
              "configuration": { "type": "object" }
            },
            "required": ["id", "type"]
          },
          "connection": {
            "type": "object",
            "properties": {
              "from": { "type": "string" },
              "to": { "type": "string" }
            },
            "required": ["from", "to"]
          }
        }
      }
      """;

  private static final String PLUGIN_CONFIG_SCHEMA = """
      {
        "type": "object",
        "properties": {
          "id": { "type": "string", "pattern": "^[a-zA-Z][a-zA-Z0-9_-]*$" },
          "name": { "type": "string" },
          "version": { "type": "string" },
          "description": { "type": "string" },
          "provider": { "type": "string" },
          "capabilities": {
            "type": "array",
            "items": { "type": "string" }
          },
          "configuration": { "type": "object" }
        },
        "required": ["id", "name", "version"]
      }
      """;

  private static final Map<String, String> SCHEMAS = new LinkedHashMap<>();

  static {
    // Core schemas
    SCHEMAS.put(WORKFLOW, WORKFLOW_SCHEMA);
    SCHEMAS.put(PLUGIN_CONFIG, PLUGIN_CONFIG_SCHEMA);

    // Discover node-provided schemas via NodeProvider SPI
    for (NodeProvider provider : ServiceLoader.load(NodeProvider.class)) {
      for (NodeDefinition node : provider.nodes()) {
        if (node.configSchema() != null && !node.configSchema().isBlank()) {
          SCHEMAS.put(node.type(), node.configSchema());
        }
      }
    }

    // Discover standalone schemas via SchemaProvider SPI (backward-compat)
    for (SchemaProvider provider : ServiceLoader.load(SchemaProvider.class)) {
      SCHEMAS.putAll(provider.schemas());
    }
  }

  private BuiltinSchemaCatalog() {
  }

  public static Set<String> ids() {
    return SCHEMAS.keySet();
  }

  public static String get(String schemaId) {
    return SCHEMAS.get(schemaId);
  }

  /**
   * Dynamically registers schemas from a NodeProvider at runtime.
   * Useful for external plugins loaded after static initialization.
   */
  public static void register(NodeProvider provider) {
    if (provider == null)
      return;
    for (NodeDefinition node : provider.nodes()) {
      if (node.configSchema() != null && !node.configSchema().isBlank()) {
        SCHEMAS.put(node.type(), node.configSchema());
      }
    }
  }
}
