package tech.kayys.silat.saga;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import tech.kayys.silat.model.NodeId;

/**
 * Compensation State - Tracks compensation progress
 */
public record CompensationState(
        List<NodeId> nodesToCompensate,
        List<NodeId> compensatedNodes,
        Instant startedAt) {
    public static CompensationState create(List<NodeId> nodes) {
        return new CompensationState(
                new ArrayList<>(nodes),
                new ArrayList<>(),
                Instant.now());
    }
}
