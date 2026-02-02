package tech.kayys.wayang.integration.designer;

import java.util.List;

public record RouteDesign(
        String routeId,
        String name,
        String description,
        String category,
        String tenantId,
        List<DesignNode> nodes,
        List<DesignConnection> connections,
        DesignMetadata metadata) {
}
