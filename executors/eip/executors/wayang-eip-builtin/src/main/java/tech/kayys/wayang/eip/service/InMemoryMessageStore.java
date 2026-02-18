package tech.kayys.wayang.eip.service;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.eip.model.MessageStore;
import tech.kayys.wayang.eip.model.StoredMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class InMemoryMessageStore implements MessageStore {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryMessageStore.class);

    private final ConcurrentHashMap<String, StoredMessage> messages = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    void init() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public Uni<String> store(Object message, Duration retention) {
        return Uni.createFrom().item(() -> {
            String messageId = UUID.randomUUID().toString();
            StoredMessage stored = new StoredMessage(
                    messageId,
                    message,
                    Instant.now(),
                    Instant.now().plus(retention));
            messages.put(messageId, stored);
            LOG.debug("Stored message: {}, expires: {}", messageId, stored.expiresAt());
            return messageId;
        });
    }

    @Override
    public Uni<Object> retrieve(String messageId) {
        return Uni.createFrom().item(() -> {
            StoredMessage stored = messages.get(messageId);
            if (stored == null) {
                throw new RuntimeException("Message not found: " + messageId); // Check valid exception usage
            }
            if (Instant.now().isAfter(stored.expiresAt())) {
                messages.remove(messageId);
                throw new RuntimeException("Message expired: " + messageId);
            }
            return stored.message();
        });
    }

    @Override
    public Uni<Boolean> delete(String messageId) {
        return Uni.createFrom().item(() -> messages.remove(messageId) != null);
    }

    private void cleanup() {
        try {
            Instant now = Instant.now();
            List<String> expired = new ArrayList<>();

            messages.forEach((id, msg) -> {
                if (now.isAfter(msg.expiresAt())) {
                    expired.add(id);
                }
            });

            expired.forEach(messages::remove);

            if (!expired.isEmpty()) {
                LOG.info("Cleaned up {} expired messages", expired.size());
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
