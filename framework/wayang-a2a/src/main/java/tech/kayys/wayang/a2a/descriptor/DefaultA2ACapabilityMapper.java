package tech.kayys.wayang.a2a.descriptor;

import tech.kayys.wayang.communication.api.AgentCapability;

/**
 * Default pass-through mapper from {@link AgentCapability} to {@link A2ASkill}.
 */
public final class DefaultA2ACapabilityMapper implements A2ACapabilityMapper {

    @Override
    public A2ASkill map(AgentCapability capability) {
        return new A2ASkill(
                capability.id().value(),
                capability.description(),
                capability.metadata()
        );
    }
}
