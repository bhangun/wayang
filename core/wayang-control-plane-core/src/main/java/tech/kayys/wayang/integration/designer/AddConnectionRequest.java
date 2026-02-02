package tech.kayys.wayang.integration.designer;

public record AddConnectionRequest(
        String sourceNodeId,
        String targetNodeId,
        String condition) {
}
