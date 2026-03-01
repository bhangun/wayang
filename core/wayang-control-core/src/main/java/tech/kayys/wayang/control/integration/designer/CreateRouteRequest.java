package tech.kayys.wayang.control.integration.designer;

public record CreateRouteRequest(
        String name,
        String description,
        String category,
        String tenantId) {
}
