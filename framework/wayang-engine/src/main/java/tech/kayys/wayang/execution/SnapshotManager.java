package tech.kayys.wayang.execution;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages execution snapshots for checkpoint and recovery.
 * 
 * <p>
 * Provides:
 * <ul>
 * <li>Snapshot creation and storage</li>
 * <li>Snapshot recovery</li>
 * <li>Snapshot lifecycle management</li>
 * <li>Snapshot retention policies</li>
 * </ul>
 */
public final class SnapshotManager {

    private final Map<UUID, ExecutionSnapshot> snapshots;
    private final Map<UUID, List<UUID>> executionHistory;
    private final SnapshotConfig config;

    public SnapshotManager() {
        this(SnapshotConfig.defaults());
    }

    public SnapshotManager(SnapshotConfig config) {
        this.snapshots = new ConcurrentHashMap<>();
        this.executionHistory = new ConcurrentHashMap<>();
        this.config = config != null ? config : SnapshotConfig.defaults();
    }

    /**
     * Creates a snapshot of the current execution state.
     */
    public ExecutionSnapshot createSnapshot(UUID executionId, ExecutionState state) {
        ExecutionSnapshot snapshot = ExecutionSnapshot.builder()
                .id(UUID.randomUUID())
                .executionId(executionId)
                .state(state)
                .createdAt(Instant.now())
                .build();

        snapshots.put(snapshot.getId(), snapshot);
        executionHistory.computeIfAbsent(executionId, k -> new ArrayList<>())
                .add(snapshot.getId());

        // Apply retention policy
        applyRetentionPolicy(executionId);

        return snapshot;
    }

    /**
     * Restores a snapshot by ID.
     */
    public Optional<ExecutionSnapshot> restoreSnapshot(UUID snapshotId) {
        return Optional.ofNullable(snapshots.get(snapshotId));
    }

    /**
     * Restores the latest snapshot for an execution.
     */
    public Optional<ExecutionSnapshot> restoreLatest(UUID executionId) {
        List<UUID> snapshotIds = executionHistory.get(executionId);
        if (snapshotIds == null || snapshotIds.isEmpty()) {
            return Optional.empty();
        }

        UUID latestId = snapshotIds.get(snapshotIds.size() - 1);
        return restoreSnapshot(latestId);
    }

    /**
     * Deletes a snapshot.
     */
    public void deleteSnapshot(UUID snapshotId) {
        ExecutionSnapshot snapshot = snapshots.remove(snapshotId);
        if (snapshot != null) {
            UUID executionId = snapshot.getExecutionId();
            List<UUID> snapshotIds = executionHistory.get(executionId);
            if (snapshotIds != null) {
                snapshotIds.remove(snapshotId);
            }
        }
    }

    /**
     * Deletes all snapshots for an execution.
     */
    public void deleteAllSnapshots(UUID executionId) {
        List<UUID> snapshotIds = executionHistory.remove(executionId);
        if (snapshotIds != null) {
            for (UUID snapshotId : snapshotIds) {
                snapshots.remove(snapshotId);
            }
        }
    }

    /**
     * Returns all snapshots for an execution.
     */
    public List<ExecutionSnapshot> getSnapshots(UUID executionId) {
        List<UUID> snapshotIds = executionHistory.get(executionId);
        if (snapshotIds == null) {
            return Collections.emptyList();
        }

        List<ExecutionSnapshot> result = new ArrayList<>();
        for (UUID snapshotId : snapshotIds) {
            ExecutionSnapshot snapshot = snapshots.get(snapshotId);
            if (snapshot != null) {
                result.add(snapshot);
            }
        }
        return result;
    }

    /**
     * Returns snapshot statistics.
     */
    public SnapshotStatistics getStatistics() {
        return SnapshotStatistics.builder()
                .totalSnapshots(snapshots.size())
                .totalExecutions(executionHistory.size())
                .retentionPeriod(config.getRetentionPeriod())
                .build();
    }

    /**
     * Clears all snapshots.
     */
    public void clear() {
        snapshots.clear();
        executionHistory.clear();
    }

    private void applyRetentionPolicy(UUID executionId) {
        List<UUID> snapshotIds = executionHistory.get(executionId);
        if (snapshotIds == null) {
            return;
        }

        int maxSnapshots = config.getMaxSnapshotsPerExecution();
        if (snapshotIds.size() > maxSnapshots) {
            // Remove oldest snapshots
            int toRemove = snapshotIds.size() - maxSnapshots;
            for (int i = 0; i < toRemove; i++) {
                UUID oldestId = snapshotIds.get(i);
                snapshots.remove(oldestId);
            }
            // Keep only the latest maxSnapshots
            snapshotIds.subList(0, toRemove).clear();
        }

        // Apply TTL
        long ttlMillis = config.getRetentionPeriod().toMillis();
        Iterator<Map.Entry<UUID, ExecutionSnapshot>> iterator = snapshots.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ExecutionSnapshot> entry = iterator.next();
            ExecutionSnapshot snapshot = entry.getValue();
            if (snapshot.getCreatedAt().plusMillis(ttlMillis).isBefore(Instant.now())) {
                iterator.remove();
                List<UUID> execSnapshots = executionHistory.get(snapshot.getExecutionId());
                if (execSnapshots != null) {
                    execSnapshots.remove(snapshot.getId());
                }
            }
        }
    }

    public static class SnapshotConfig {
        private final int maxSnapshotsPerExecution;
        private final java.time.Duration retentionPeriod;

        public SnapshotConfig(int maxSnapshotsPerExecution, java.time.Duration retentionPeriod) {
            this.maxSnapshotsPerExecution = maxSnapshotsPerExecution;
            this.retentionPeriod = retentionPeriod;
        }

        public int getMaxSnapshotsPerExecution() {
            return maxSnapshotsPerExecution;
        }

        public java.time.Duration getRetentionPeriod() {
            return retentionPeriod;
        }

        public static SnapshotConfig defaults() {
            return new SnapshotConfig(10, java.time.Duration.ofDays(7));
        }
    }

    public static class SnapshotStatistics {
        private final int totalSnapshots;
        private final int totalExecutions;
        private final java.time.Duration retentionPeriod;

        public SnapshotStatistics(int totalSnapshots, int totalExecutions, java.time.Duration retentionPeriod) {
            this.totalSnapshots = totalSnapshots;
            this.totalExecutions = totalExecutions;
            this.retentionPeriod = retentionPeriod;
        }

        public int getTotalSnapshots() {
            return totalSnapshots;
        }

        public int getTotalExecutions() {
            return totalExecutions;
        }

        public java.time.Duration getRetentionPeriod() {
            return retentionPeriod;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int totalSnapshots;
            private int totalExecutions;
            private java.time.Duration retentionPeriod;

            public Builder totalSnapshots(int totalSnapshots) {
                this.totalSnapshots = totalSnapshots;
                return this;
            }

            public Builder totalExecutions(int totalExecutions) {
                this.totalExecutions = totalExecutions;
                return this;
            }

            public Builder retentionPeriod(java.time.Duration retentionPeriod) {
                this.retentionPeriod = retentionPeriod;
                return this;
            }

            public SnapshotStatistics build() {
                return new SnapshotStatistics(totalSnapshots, totalExecutions, retentionPeriod);
            }
        }
    }
}
