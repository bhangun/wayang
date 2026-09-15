package tech.kayys.wayang.security.delegation;

/**
 * Attenuates a delegation to ensure child authority is always a subset of parent authority.
 *
 * <p>Invariant: {@code child ⊆ parent} for all constraint dimensions.
 */
public interface DelegationAttenuator {

    /**
     * Computes the effective child constraints, which are the intersection of
     * what the parent allows and what the child requests.
     *
     * @param parent    the parent delegation's constraints
     * @param requested the constraints requested for the child
     * @return the attenuated (intersected) constraints
     */
    DelegationConstraints attenuate(
            DelegationConstraints parent,
            DelegationConstraints requested
    );
}
