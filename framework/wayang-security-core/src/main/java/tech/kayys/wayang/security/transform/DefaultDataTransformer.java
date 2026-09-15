package tech.kayys.wayang.security.transform;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DefaultDataTransformer implements DataTransformer {

    @Override
    @SuppressWarnings("unchecked")
    public CompletionStage<TransformationResult> transform(TransformationRequest request) {
        if (request.data() == null) {
            return CompletableFuture.completedFuture(TransformationResult.unchanged(null));
        }

        Object data = request.data();
        List<String> fields = (List<String>) request.parameters().getOrDefault("fields", List.of());

        if (data instanceof Map<?, ?> map && !fields.isEmpty()) {
            Map<String, Object> transformed = new LinkedHashMap<>();
            map.forEach((k, v) -> transformed.put(String.valueOf(k), v));

            boolean modified = false;
            for (String field : fields) {
                if (transformed.containsKey(field)) {
                    modified = true;
                    switch (request.operation()) {
                        case REDACT -> transformed.remove(field);
                        case MASK -> transformed.put(field, "****");
                        case REPLACE -> transformed.put(field, request.parameters().getOrDefault("replacement", "[REPLACED]"));
                        default -> {}
                    }
                }
            }

            return CompletableFuture.completedFuture(
                    modified ? TransformationResult.modified(Collections.unmodifiableMap(transformed))
                             : TransformationResult.unchanged(data)
            );
        }

        return CompletableFuture.completedFuture(TransformationResult.unchanged(data));
    }
}
