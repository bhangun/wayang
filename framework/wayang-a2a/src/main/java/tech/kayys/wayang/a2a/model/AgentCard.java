package tech.kayys.wayang.a2a.model;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Represents the identity, capabilities, and endpoint of a remote agent.
 */
public record AgentCard(
    String id,
    String name,
    String description,
    List<String> capabilities,
    List<String> skills,
    List<String> modalities,
    Map<String, String> authentication,
    URI endpoint,
    Map<String, Object> metadata
) {}
