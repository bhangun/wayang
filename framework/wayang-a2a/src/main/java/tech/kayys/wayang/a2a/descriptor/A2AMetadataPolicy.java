package tech.kayys.wayang.a2a.descriptor;

import tech.kayys.wayang.communication.api.AgentDescriptor;

import java.util.Map;

/**
 * Controls which metadata fields from {@link AgentDescriptor} are exposed in the public A2A Agent Card.
 * When disabled, returns an empty map to prevent leaking internal details.
 */
public final class A2AMetadataPolicy {

    private A2AMetadataPolicy() {}

    public static Map<String, Object> expose(AgentDescriptor descriptor, boolean enabled) {
        if (!enabled) {
            return Map.of();
        }
        return descriptor.metadata();
    }
}
