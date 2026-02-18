package tech.kayys.wayang.eip.service;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.eip.config.AggregatorConfig;
import tech.kayys.wayang.eip.model.Aggregation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class AggregatorStore {

    private static final Logger LOG = LoggerFactory.getLogger(AggregatorStore.class);

    private final ConcurrentHashMap<String, Aggregation> aggregations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    void init() {
        // Schedule cleanup task every minute
        cleanupScheduler.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    public Uni<Aggregation> add(String correlationId, Object message, AggregatorConfig config) {
        return Uni.createFrom().item(() -> {
            Aggregation updated = aggregations.compute(correlationId, (key, existing) -> {
                if (existing == null) {
                    List<Object> messages = Collections.synchronizedList(new ArrayList<>());
                    messages.add(message);
                    return new Aggregation(
                            correlationId,
                            messages,
                            Instant.now(),
                            Instant.now().plus(config.timeout()),
                            config.expectedCount());
                } else {
                    existing.messages().add(message);
                    return existing;
                }
            });

            return updated;
        });
    }

    public Uni<List<Object>> remove(String correlationId) {
        return Uni.createFrom().item(() -> {
            Aggregation aggregation = aggregations.remove(correlationId);
            if (aggregation != null) {
                return new ArrayList<>(aggregation.messages());
            }
            return List.of();
        });
    }

    private void cleanup() {
        try {
            Instant now = Instant.now();
            List<String> expired = new ArrayList<>();

            aggregations.forEach((id, agg) -> {
                if (now.isAfter(agg.expiresAt())) {
                    expired.add(id);
                }
            });

            expired.forEach(id -> {
                Aggregation agg = aggregations.remove(id);
                if (agg != null) {
                    LOG.warn("Removed expired aggregation: {}, had {} messages",
                            id, agg.messages().size());
                }
            });

        } catch (Exception e) {
            LOG.error("Cleanup failed", e);
        }
    }

    @PreDestroy
    void shutdown() {
        cleanupScheduler.shutdown();
    }
}
