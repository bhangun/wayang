package tech.kayys.wayang.integration.core.service;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.integration.core.model.IdempotencyRecord;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class IdempotencyStore {

    private static final Logger LOG = LoggerFactory.getLogger(IdempotencyStore.class);

    private final ConcurrentHashMap<String, IdempotencyRecord> records = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    void init() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.HOURS);
    }

    public Uni<Boolean> checkAndRecord(String key, Duration window) {
        return Uni.createFrom().item(() -> {
            Instant now = Instant.now();
            Instant expiresAt = now.plus(window);

            IdempotencyRecord existing = records.get(key);

            if (existing != null) {
                if (now.isBefore(existing.expiresAt())) {
                    LOG.debug("Duplicate detected for key: {}", key);
                    return true; // Duplicate
                } else {
                    // Expired - treat as new
                    records.put(key, new IdempotencyRecord(key, now, expiresAt));
                    return false;
                }
            } else {
                // New key
                records.put(key, new IdempotencyRecord(key, now, expiresAt));
                return false;
            }
        });
    }

    private void cleanup() {
        try {
            Instant now = Instant.now();
            int removed = 0;

            Iterator<Map.Entry<String, IdempotencyRecord>> iterator = records.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, IdempotencyRecord> entry = iterator.next();
                if (now.isAfter(entry.getValue().expiresAt())) {
                    iterator.remove();
                    removed++;
                }
            }

            if (removed > 0) {
                LOG.info("Cleaned up {} expired idempotency records", removed);
            }
        } catch (Exception e) {
            LOG.error("Cleanup failed", e);
        }
    }

    @PreDestroy
    void shutdown() {
        cleanupScheduler.shutdown();
    }
}
