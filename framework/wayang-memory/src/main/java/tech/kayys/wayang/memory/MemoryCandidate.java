package tech.kayys.wayang.memory;

import java.util.List;

/**
 * A request from a strategy/agent to store something in memory.
 */
public record MemoryCandidate(
    String content,
    String type,
    MemoryPolicy policy,
    List<String> provenanceReferences
) {}
