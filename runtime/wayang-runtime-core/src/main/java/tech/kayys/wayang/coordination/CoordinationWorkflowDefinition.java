package tech.kayys.wayang.coordination;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Compiled workflow definition IR ready for Gamelan or local execution.
 */
public record CoordinationWorkflowDefinition(
        String name,
        String backend,
        List<Node> nodes,
        List<Edge> edges,
        Map<String, Object> metadata
) {

    public CoordinationWorkflowDefinition {
        Objects.requireNonNull(name, "name must not be null");
        backend = backend == null ? "gamelan" : backend;
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public record Node(
            String id,
            String type,
            String agentId,
            String operation,
            List<String> dependsOn,
            Map<String, Object> parameters,
            Map<String, Object> metadata
    ) {

        public Node {
            Objects.requireNonNull(id, "node id must not be null");
            type = type == null ? "agent" : type;
            agentId = agentId == null ? "" : agentId;
            operation = operation == null ? "execute" : operation;
            dependsOn = dependsOn == null ? List.of() : List.copyOf(dependsOn);
            parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    public record Edge(
            String from,
            String to,
            String label,
            Map<String, Object> metadata
    ) {

        public Edge {
            Objects.requireNonNull(from, "from must not be null");
            Objects.requireNonNull(to, "to must not be null");
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }
}
