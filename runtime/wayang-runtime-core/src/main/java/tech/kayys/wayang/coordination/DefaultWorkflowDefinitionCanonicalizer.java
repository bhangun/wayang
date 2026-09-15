package tech.kayys.wayang.coordination;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DefaultWorkflowDefinitionCanonicalizer implements WorkflowDefinitionCanonicalizer {

    private final ObjectMapper objectMapper;

    @Inject
    public DefaultWorkflowDefinitionCanonicalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public DefaultWorkflowDefinitionCanonicalizer() {
        this(new ObjectMapper());
    }

    @Override
    public String canonicalize(CoordinationWorkflowDefinition definition) {
        ObjectNode root = objectMapper.createObjectNode();

        root.put("name", definition.name());
        root.put("backend", definition.backend());

        ArrayNode nodes = root.putArray("nodes");
        definition.nodes().stream()
                .sorted(Comparator.comparing(CoordinationWorkflowDefinition.Node::id))
                .forEach(node -> nodes.add(canonicalNode(node)));

        ArrayNode edges = root.putArray("edges");
        definition.edges().stream()
                .sorted(Comparator.comparing(CoordinationWorkflowDefinition.Edge::from)
                        .thenComparing(CoordinationWorkflowDefinition.Edge::to)
                        .thenComparing(edge -> edge.label() == null ? "" : edge.label()))
                .forEach(edge -> edges.add(canonicalEdge(edge)));

        return root.toString();
    }

    private ObjectNode canonicalNode(CoordinationWorkflowDefinition.Node node) {
        ObjectNode result = objectMapper.createObjectNode();

        result.put("id", node.id());
        result.put("type", node.type());
        result.put("agentId", node.agentId());
        result.put("operation", node.operation());

        ArrayNode dependsOn = result.putArray("dependsOn");
        node.dependsOn().stream()
                .sorted()
                .forEach(dependsOn::add);

        result.set("parameters", canonicalValue(node.parameters()));
        result.set("metadata", canonicalValue(node.metadata()));

        return result;
    }

    private ObjectNode canonicalEdge(CoordinationWorkflowDefinition.Edge edge) {
        ObjectNode result = objectMapper.createObjectNode();

        result.put("from", edge.from());
        result.put("to", edge.to());
        if (edge.label() != null) {
            result.put("label", edge.label());
        }
        result.set("metadata", canonicalValue(edge.metadata()));

        return result;
    }

    private JsonNode canonicalValue(Object value) {
        if (value == null) {
            return objectMapper.nullNode();
        }

        if (value instanceof Map<?, ?> map) {
            ObjectNode object = objectMapper.createObjectNode();
            map.entrySet().stream()
                    .sorted(Comparator.comparing(entry -> String.valueOf(entry.getKey())))
                    .forEach(entry -> object.set(String.valueOf(entry.getKey()), canonicalValue(entry.getValue())));
            return object;
        }

        if (value instanceof Iterable<?> iterable) {
            List<JsonNode> values = new ArrayList<>();
            for (Object item : iterable) {
                values.add(canonicalValue(item));
            }
            ArrayNode array = objectMapper.createArrayNode();
            values.forEach(array::add);
            return array;
        }

        return objectMapper.valueToTree(value);
    }
}
