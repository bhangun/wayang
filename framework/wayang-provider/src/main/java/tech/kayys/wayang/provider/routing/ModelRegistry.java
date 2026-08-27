package tech.kayys.wayang.provider.routing;

import java.util.List;
import java.util.Optional;

/**
 * Catalog of known model specifications, capabilities, context limits, and pricing.
 */
public interface ModelRegistry {

    List<ModelSpec> listModels();

    Optional<ModelSpec> findModel(String modelId);

    void registerModel(ModelSpec spec);
}
