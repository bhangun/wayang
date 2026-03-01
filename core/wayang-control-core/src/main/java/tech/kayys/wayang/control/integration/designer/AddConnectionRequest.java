package tech.kayys.wayang.control.integration.designer;

public record AddConnectionRequest(
        String sourceNodeId,
        String targetNodeId,
        String condition) {
}
