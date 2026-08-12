package tech.kayys.wayang.a2a.model;

import java.util.List;

/**
 * Represents a message in the A2A protocol.
 */
public record A2AMessage(
    Role role,
    List<A2APart> content
) {
    public enum Role {
        USER,
        ASSISTANT,
        SYSTEM
    }
}
