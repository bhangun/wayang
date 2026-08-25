package tech.kayys.wayang.memory;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A portable, self-describing snapshot of all memory records
 * across one or more scopes/tiers.
 *
 * <p>Can be serialized to/from JSON (NDJSON per record for streaming)
 * for cross-agent, cross-instance, and cross-backend portability.
 */
public record MemorySnapshot(
        /** Schema version for forward-compat migration. */
        int schemaVersion,
        /** Snapshot identifier — stable across re-exports of the same agent. */
        String snapshotId,
        /** Agent or namespace this snapshot belongs to. */
        String agentId,
        /** User identifier if applicable. */
        String userId,
        /** Tenant identifier if applicable. */
        String tenantId,
        /** Wall-clock time when the snapshot was captured. */
        Instant capturedAt,
        /** Memory tiers included in this snapshot. */
        List<String> tiers,
        /** Memory records across all included tiers. */
        List<MemoryRecord> records,
        /** Optional metadata: tool versions, model, environment. */
        Map<String, Object> exportMetadata
) {
    public static final int CURRENT_SCHEMA = 1;

    public MemorySnapshot {
        records = records != null ? List.copyOf(records) : List.of();
        tiers   = tiers   != null ? List.copyOf(tiers)   : List.of();
        exportMetadata = exportMetadata != null ? Map.copyOf(exportMetadata) : Map.of();
        if (capturedAt == null) capturedAt = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String snapshotId;
        private String agentId;
        private String userId;
        private String tenantId;
        private Instant capturedAt;
        private List<String> tiers;
        private List<MemoryRecord> records;
        private Map<String, Object> exportMetadata;

        public Builder snapshotId(String id)              { this.snapshotId = id; return this; }
        public Builder agentId(String id)                 { this.agentId = id; return this; }
        public Builder userId(String id)                  { this.userId = id; return this; }
        public Builder tenantId(String id)                { this.tenantId = id; return this; }
        public Builder capturedAt(Instant t)              { this.capturedAt = t; return this; }
        public Builder tiers(List<String> t)              { this.tiers = t; return this; }
        public Builder records(List<MemoryRecord> r)      { this.records = r; return this; }
        public Builder exportMetadata(Map<String, Object> m) { this.exportMetadata = m; return this; }

        public MemorySnapshot build() {
            return new MemorySnapshot(
                CURRENT_SCHEMA, snapshotId, agentId, userId, tenantId,
                capturedAt, tiers, records, exportMetadata
            );
        }
    }
}
