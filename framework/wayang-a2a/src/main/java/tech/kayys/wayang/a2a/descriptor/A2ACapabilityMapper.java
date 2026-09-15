package tech.kayys.wayang.a2a.descriptor;

import tech.kayys.wayang.communication.api.AgentCapability;

/**
 * Translates a Wayang {@link AgentCapability} into an A2A {@link A2ASkill}.
 * Implementations can filter, enrich or transform capabilities as needed.
 */
public interface A2ACapabilityMapper {

    A2ASkill map(AgentCapability capability);
}
