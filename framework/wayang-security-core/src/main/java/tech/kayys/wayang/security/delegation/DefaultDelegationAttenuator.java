package tech.kayys.wayang.security.delegation;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link DelegationAttenuator} that intersects capabilities and actions.
 */
public final class DefaultDelegationAttenuator implements DelegationAttenuator {

    @Override
    public DelegationConstraints attenuate(
            DelegationConstraints parent,
            DelegationConstraints requested) {

        List<String> capabilities = intersect(parent.allowedCapabilities(), requested.allowedCapabilities());
        List<String> actions = intersect(parent.allowedActions(), requested.allowedActions());
        int maxHops = Math.min(Math.max(0, parent.maxHops() - 1), requested.maxHops());

        return new DelegationConstraints(capabilities, actions, maxHops);
    }

    private List<String> intersect(List<String> parent, List<String> requested) {
        if (parent.contains("*")) {
            return requested;
        }
        if (requested.contains("*")) {
            return parent;
        }
        List<String> result = new ArrayList<>(parent);
        result.retainAll(requested);
        return List.copyOf(result);
    }
}
