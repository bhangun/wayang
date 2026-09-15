package tech.kayys.wayang.security.propagation;

/**
 * Service for creating attenuated security contexts when delegating invocations across agents.
 */
public interface SecurityContextPropagator {

    PropagationResult propagate(PropagationRequest request);
}
