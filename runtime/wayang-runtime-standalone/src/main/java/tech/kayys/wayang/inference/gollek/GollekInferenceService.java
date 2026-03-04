package tech.kayys.wayang.inference.gollek;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gollek.engine.inference.InferenceService;
import io.smallrye.mutiny.Uni;

/**
 * Wrapper service for Gollek inference engine integration with Wayang standalone runtime.
 */
@ApplicationScoped
public class GollekInferenceService {

    private static final Logger LOG = LoggerFactory.getLogger(GollekInferenceService.class);

    @Inject
    Instance<InferenceService> inferenceService;

    public void initialize() {
        LOG.info("Gollek Inference Service initialized in standalone mode");
    }

    public boolean isAvailable() {
        boolean available = inferenceService != null && inferenceService.isResolvable();
        if (available) {
            LOG.debug("Gollek Inference Service is available");
        } else {
            LOG.warn("Gollek Inference Service not available");
        }
        return available;
    }

    public Uni<String> testConnection() {
        return Uni.createFrom().item(() -> {
            if (isAvailable()) {
                LOG.info("Successfully connected to Gollek Inference Engine");
                return "Gollek Inference Engine connected";
            } else {
                LOG.warn("Could not connect to Gollek Inference Engine");
                return "Gollek Inference Engine not available";
            }
        });
    }
}
