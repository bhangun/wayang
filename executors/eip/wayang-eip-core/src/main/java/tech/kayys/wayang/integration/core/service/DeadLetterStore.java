package tech.kayys.wayang.integration.core.service;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.integration.core.model.DeadLetterMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
public class DeadLetterStore {

    private static final Logger LOG = LoggerFactory.getLogger(DeadLetterStore.class);

    private final ConcurrentHashMap<String, List<DeadLetterMessage>> channels = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    void init() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.HOURS);
    }

    public Uni<Boolean> store(String channelName, DeadLetterMessage message) {
        return Uni.createFrom().item(() -> {
            List<DeadLetterMessage> channel = channels.computeIfAbsent(
                    channelName,
                    k -> Collections.synchronizedList(new ArrayList<>()));

            channel.add(message);

            LOG.info("Stored dead letter: {} in channel: {}", message.id(), channelName);

            return true;
        });
    }

    public Uni<List<DeadLetterMessage>> list(String channelName) {
        return Uni.createFrom().item(() -> {
            List<DeadLetterMessage> channel = channels.get(channelName);
            return channel != null ? new ArrayList<>(channel) : List.of();
        });
    }

    public Uni<DeadLetterMessage> get(String channelName, String messageId) {
        return Uni.createFrom().item(() -> {
            List<DeadLetterMessage> channel = channels.get(channelName);
            if (channel != null) {
                return channel.stream()
                        .filter(msg -> msg.id().equals(messageId))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        });
    }

    public Uni<Boolean> retry(String channelName, String messageId) {
        return Uni.createFrom().item(() -> {
            List<DeadLetterMessage> channel = channels.get(channelName);
            if (channel != null) {
                DeadLetterMessage message = channel.stream()
                        .filter(msg -> msg.id().equals(messageId))
                        .findFirst()
                        .orElse(null);

                if (message != null) {
                    channel.remove(message);
                    LOG.info("Removed dead letter for retry: {}", messageId);

                    // In production, re-inject into processing queue
                    return true;
                }
            }
            return false;
        });
    }

    public Uni<Boolean> delete(String channelName, String messageId) {
        return Uni.createFrom().item(() -> {
            List<DeadLetterMessage> channel = channels.get(channelName);
            if (channel != null) {
                boolean removed = channel.removeIf(msg -> msg.id().equals(messageId));
                if (removed) {
                    LOG.info("Deleted dead letter: {}", messageId);
                }
                return removed;
            }
            return false;
        });
    }

    public Uni<Map<String, Object>> getStatistics(String channelName) {
        return Uni.createFrom().item(() -> {
            List<DeadLetterMessage> channel = channels.get(channelName);
            if (channel == null) {
                return Map.of("channelName", channelName, "count", 0);
            }

            long total = channel.size();
            long expired = channel.stream()
                    .filter(msg -> Instant.now().isAfter(msg.expiresAt()))
                    .count();

            // Error type distribution
            Map<String, Long> errorTypes = channel.stream()
                    .collect(Collectors.groupingBy(
                            msg -> msg.errorDetails().getOrDefault("errorType", "Unknown").toString(),
                            Collectors.counting()));

            // Recent messages
            List<Map<String, Object>> recent = channel.stream()
                    .sorted(Comparator.comparing(DeadLetterMessage::timestamp).reversed())
                    .limit(10)
                    .map(msg -> Map.of(
                            "id", msg.id(),
                            "timestamp", msg.timestamp().toString(),
                            "nodeId", msg.nodeId(),
                            "errorType", msg.errorDetails().getOrDefault("errorType", "Unknown")))
                    .toList();

            return Map.of(
                    "channelName", channelName,
                    "totalCount", total,
                    "expiredCount", expired,
                    "errorTypes", errorTypes,
                    "recentMessages", recent);
        });
    }

    private void cleanup() {
        try {
            Instant now = Instant.now();
            int totalRemoved = 0;

            for (Map.Entry<String, List<DeadLetterMessage>> entry : channels.entrySet()) {
                String channelName = entry.getKey();
                List<DeadLetterMessage> messages = entry.getValue();

                int beforeSize = messages.size();
                messages.removeIf(msg -> now.isAfter(msg.expiresAt()));
                int removed = beforeSize - messages.size();

                if (removed > 0) {
                    totalRemoved += removed;
                    LOG.info("Cleaned up {} expired dead letters from channel: {}",
                            removed, channelName);
                }
            }

            if (totalRemoved > 0) {
                LOG.info("Total dead letters cleaned up: {}", totalRemoved);
            }
        } catch (Exception e) {
            LOG.error("Dead letter cleanup failed", e);
        }
    }

    @PreDestroy
    void shutdown() {
        cleanupScheduler.shutdown();
    }
}
