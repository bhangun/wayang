package tech.kayys.wayang.integration.designer;

public record CreateRouteRequest(
        String name,
        String description,
        String category,
        String tenantId) {
}
