package tech.kayys.wayang.agent.spi;

import java.util.List;
import java.util.Map;

/**
 * Service provider interface for backend discovery.
 */
public interface BackendProvider {

    String name();

    default int priority() {
        return 0;
    }

    InferenceBackend createInferenceBackend(Map<String, Object> config);

    default WorkflowBackend createWorkflowBackend(Map<String, Object> config) {
        return null;
    }

    default boolean isAvailable() {
        return true;
    }

    List<String> supportedBackends();
}
