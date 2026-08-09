package tech.kayys.wayang.tool.capability;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a complex request/filter used by an Agent to query the ToolRegistry.
 */
public class CapabilityRequest {
    private final Set<Capability> requiredCapabilities = new HashSet<>();

    public CapabilityRequest require(Capability capability) {
        requiredCapabilities.add(capability);
        return this;
    }

    public Collection<Capability> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    /**
     * Checks if a given set of tool capabilities satisfies this request.
     */
    public boolean isSatisfiedBy(Collection<Capability> toolCapabilities) {
        if (requiredCapabilities.isEmpty()) {
            return true;
        }
        for (Capability req : requiredCapabilities) {
            boolean satisfied = false;
            for (Capability provided : toolCapabilities) {
                if (provided.id().equals(req.id()) && provided.satisfies(req)) {
                    satisfied = true;
                    break;
                }
            }
            if (!satisfied) {
                return false;
            }
        }
        return true;
    }
}
