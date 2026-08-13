package tech.kayys.wayang.a2a.distributed;

import tech.kayys.wayang.a2a.durable.DurableA2ATask;
import tech.kayys.wayang.a2a.durable.DurableA2ATaskLedger;
import tech.kayys.wayang.a2a.model.A2ATaskStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Relays execution events across agent boundaries (Phase 8 §44 — Distributed event/observability).
 *
 * <p>When a local execution fires an A2A call, the calling agent publishes events to the
 * remote. The remote publishes status updates back. This relay is the local switchboard:
 * <ul>
 *   <li>Subscribers (e.g. monitoring dashboards, other agents) register via {@link #subscribe}.</li>
 *   <li>The transport layer calls {@link #onRemoteStatusUpdate} when it receives status from remote.</li>
 *   <li>The relay updates the {@link DurableA2ATaskLedger} and notifies subscribers.</li>
 * </ul>
 *
 * <p>This is an in-process relay suitable for single-node deployment. For true distributed
 * observability, replace with a pub-sub backend (Kafka, Redis Streams, etc.) by implementing
 * the same interface with a different transport.</p>
 */
public class DistributedEventRelay {

    private static final Logger LOG = Logger.getLogger(DistributedEventRelay.class.getName());

    /** Immutable event envelope crossing agent boundaries. */
    public record RelayEvent(
        String    relayId,
        String    sourceAgentId,
        String    targetAgentId,
        String    taskId,
        String    eventType,
        Map<String, Object> payload,
        Instant   timestamp
    ) {
        static RelayEvent of(String sourceAgentId, String targetAgentId,
                             String taskId, String eventType, Map<String, Object> payload) {
            return new RelayEvent(UUID.randomUUID().toString(),
                sourceAgentId, targetAgentId, taskId, eventType, Map.copyOf(payload),
                Instant.now());
        }
    }

    private final DurableA2ATaskLedger ledger;
    private final String localAgentId;

    /** Registered event consumers. Thread-safe for concurrent subscription. */
    private final CopyOnWriteArrayList<Consumer<RelayEvent>> subscribers = new CopyOnWriteArrayList<>();

    /** In-memory relay log (bounded). */
    private static final int MAX_LOG = 1000;
    private final List<RelayEvent> eventLog = new ArrayList<>();

    public DistributedEventRelay(DurableA2ATaskLedger ledger, String localAgentId) {
        this.ledger       = ledger;
        this.localAgentId = localAgentId;
    }

    // ── Subscription ──────────────────────────────────────────────────────────

    public void subscribe(Consumer<RelayEvent> handler) {
        subscribers.add(handler);
    }

    public void unsubscribe(Consumer<RelayEvent> handler) {
        subscribers.remove(handler);
    }

    // ── Publish (outbound to remote agent) ────────────────────────────────────

    /**
     * Publishes a control event toward a remote agent.
     * In a real deployment, this would serialize and send over HTTP/gRPC/Kafka.
     */
    public void publish(String targetAgentId, String taskId, String eventType, Map<String, Object> payload) {
        RelayEvent event = RelayEvent.of(localAgentId, targetAgentId, taskId, eventType, payload);
        log(event);
        notifySubscribers(event);
        LOG.fine(() -> "Relay PUBLISH → " + targetAgentId + " task=" + taskId + " type=" + eventType);
    }

    /** Convenience: publish a CANCEL_REQUESTED event. */
    public void publishCancel(String targetAgentId, String taskId) {
        publish(targetAgentId, taskId, "CANCEL_REQUESTED",
            Map.of("reason", "caller-initiated-cancel", "callerAgent", localAgentId));
    }

    // ── Receive (inbound from remote agent) ───────────────────────────────────

    /**
     * Entry point for the transport layer when the remote sends a status update.
     * Updates the local {@link DurableA2ATaskLedger} and notifies subscribers.
     */
    public void onRemoteStatusUpdate(String taskId, String rawStatus, Map<String, Object> payload) {
        A2ATaskStatus status;
        try {
            status = A2ATaskStatus.valueOf(rawStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOG.warning("Unknown status from remote: " + rawStatus + " for task " + taskId);
            return;
        }

        // Update ledger
        DurableA2ATask updated = ledger.update(taskId, status);
        if (updated == null) {
            LOG.warning("Received status update for unknown task: " + taskId);
            return;
        }

        // Update event seq if provided
        Object seq = payload.get("lastEventSeq");
        if (seq instanceof Number n) {
            ledger.updateEventSeq(taskId, n.longValue());
        }

        // Relay inbound event to local subscribers
        RelayEvent event = RelayEvent.of(
            payload.getOrDefault("sourceAgent", "unknown").toString(),
            localAgentId, taskId, "REMOTE_STATUS_UPDATE",
            Map.of("status", rawStatus, "payload", payload)
        );
        log(event);
        notifySubscribers(event);

        LOG.info(() -> "Relay RECEIVE task=" + taskId + " status=" + status);
    }

    /**
     * Entry point for the transport layer when the remote sends a CHECKPOINT_SAVED event.
     * Updates the checkpoint anchor in the local ledger.
     */
    public void onRemoteCheckpointSaved(String taskId, String checkpointId) {
        ledger.updateCheckpoint(taskId, checkpointId);
        RelayEvent event = RelayEvent.of(
            "remote", localAgentId, taskId, "REMOTE_CHECKPOINT_SAVED",
            Map.of("checkpointId", checkpointId));
        log(event);
        notifySubscribers(event);
        LOG.fine(() -> "Relay CHECKPOINT task=" + taskId + " checkpoint=" + checkpointId);
    }

    // ── Introspection ─────────────────────────────────────────────────────────

    public synchronized List<RelayEvent> recentEvents() {
        return List.copyOf(eventLog);
    }

    public int subscriberCount() { return subscribers.size(); }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void notifySubscribers(RelayEvent event) {
        for (Consumer<RelayEvent> sub : subscribers) {
            try { sub.accept(event); }
            catch (Exception e) {
                LOG.warning("Subscriber threw on relay event: " + e.getMessage());
            }
        }
    }

    private synchronized void log(RelayEvent event) {
        if (eventLog.size() >= MAX_LOG) eventLog.remove(0);
        eventLog.add(event);
    }
}
