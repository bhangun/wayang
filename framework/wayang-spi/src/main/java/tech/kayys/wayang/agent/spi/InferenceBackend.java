package tech.kayys.wayang.agent.spi;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Map;

/**
 * Backend-agnostic inference interface.
 */
public interface InferenceBackend {

    String name();

    String version();

    Uni<InferenceResponse> infer(InferenceRequest request);

    Multi<InferenceTypes.StreamingChunk> stream(InferenceRequest request);

    List<InferenceTypes.ProviderInfo> listProviders();

    boolean isHealthy();

    default BackendCapabilities capabilitiesInfo() {
        return BackendCapabilities.none();
    }

    default void initialize(Map<String, Object> config) {
    }

    default void shutdown() {
    }

    default boolean supports(long capabilities) {
        return (capabilities() & capabilities) == capabilities;
    }

    long CAP_STREAMING = 1L << 0;
    long CAP_TOOL_CALLING = 1L << 1;
    long CAP_MULTIMODAL = 1L << 2;
    long CAP_STRUCTURED_OUTPUT = 1L << 3;
    long CAP_PARALLEL_TOOLS = 1L << 4;
    long CAP_VISION = 1L << 5;
    long CAP_AUDIO = 1L << 6;
    long CAP_EMBEDDING = 1L << 7;

    default long capabilities() {
        return 0;
    }

    default Map<String, Object> defaultParameters() {
        return Map.of(
            "temperature", 0.7,
            "max_tokens", 2048
        );
    }
}
