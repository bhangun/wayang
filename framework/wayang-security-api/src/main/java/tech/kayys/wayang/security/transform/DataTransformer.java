package tech.kayys.wayang.security.transform;

import java.util.concurrent.CompletionStage;

/**
 * SPI for transforming data (e.g. redacting or masking fields) based on policy obligations.
 */
public interface DataTransformer {

    CompletionStage<TransformationResult> transform(TransformationRequest request);
}
