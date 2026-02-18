package tech.kayys.wayang.eip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.eip.config.EnrichmentSource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class EnrichmentService {

    private static final Logger LOG = LoggerFactory.getLogger(EnrichmentService.class);

    @Inject
    ObjectMapper objectMapper;

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    public Uni<Map<String, Object>> enrich(EnrichmentSource source, Object message, Map<String, Object> context) {
        LOG.debug("Enriching from source: {} ({})", source.type(), source.uri());

        return (Uni<Map<String, Object>>) (Uni<?>) switch (source.type()) {
            case "cache" -> enrichFromCache(source, message);
            case "static" -> enrichFromStatic(source, message);
            case "context" -> enrichFromContext(source, context);
            default -> Uni.createFrom().item(new HashMap<String, Object>());
        };
    }

    private Uni<Map<String, Object>> enrichFromCache(EnrichmentSource source, Object message) {
        return Uni.createFrom().item(() -> {
            String key = extractKey(source, message);
            Object cached = cache.get(key);

            if (cached != null) {
                return Map.of("cached", cached, "cacheHit", true);
            }
            return Map.of("cacheHit", false);
        });
    }

    private Uni<Map<String, Object>> enrichFromStatic(EnrichmentSource source, Object message) {
        return Uni.createFrom().item(() -> {
            // Parse static data from URI
            try {
                if (source.uri().startsWith("json:")) {
                    String json = source.uri().substring(5);
                    Map<String, Object> result = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
                    });
                    return result;
                }
                return (Map<String, Object>) (Map<?, ?>) source.mapping();
            } catch (Exception e) {
                LOG.error("Failed to parse static enrichment", e);
                return new HashMap<String, Object>();
            }
        });
    }

    private Uni<Map<String, Object>> enrichFromContext(EnrichmentSource source, Map<String, Object> context) {
        return Uni.createFrom().item(() -> {
            Map<String, Object> enrichment = new HashMap<>();
            source.mapping().forEach((targetKey, sourceKey) -> {
                Object value = context.get(sourceKey);
                if (value != null) {
                    enrichment.put(targetKey, value);
                }
            });
            return enrichment;
        });
    }

    private String extractKey(EnrichmentSource source, Object message) {
        if (message instanceof Map) {
            Object keyField = ((Map<?, ?>) message).get(source.uri());
            return keyField != null ? keyField.toString() : "default";
        }
        return message.toString();
    }

    public void putCache(String key, Object value) {
        cache.put(key, value);
    }
}
