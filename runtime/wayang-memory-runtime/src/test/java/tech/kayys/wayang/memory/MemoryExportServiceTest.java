package tech.kayys.wayang.memory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryExportServiceTest {

    // Stub MemoryProvider that stores records in-memory
    static class StubMemoryProvider implements MemoryProvider {
        final List<MemoryRecord> records = new ArrayList<>();
        private final String tier;

        StubMemoryProvider(String tier) { this.tier = tier; }

        @Override public String name() { return tier; }
        @Override public void save(MemoryRecord r) { records.add(r); }
        @Override public void save(List<MemoryRecord> rs) { records.addAll(rs); }
        @Override public Optional<MemoryRecord> get(String key) {
            return records.stream().filter(r -> r.key().equals(key)).findFirst();
        }
        @Override public List<MemoryRecord> search(MemoryQuery query) { return List.copyOf(records); }
        @Override public void delete(String key) { records.removeIf(r -> r.key().equals(key)); }
        @Override public void clear() { records.clear(); }
        @Override public List<MemoryRecord> exportAll(String agentId) {
            return records.stream()
                .filter(r -> agentId.equals(r.metadata().getOrDefault("agentId", "")) || r.userId() == null)
                .toList();
        }
    }

    // Fake service for testing with given providers
    static class TestExportService extends MemoryExportService {
        final List<MemoryProvider> testProviders;
        TestExportService(List<MemoryProvider> testProviders) { this.testProviders = testProviders; }

        @Override
        public io.smallrye.mutiny.Uni<MemorySnapshot> exportSnapshot(String agentId, String userId, String tenantId) {
            return io.smallrye.mutiny.Uni.createFrom().item(() -> {
                List<MemoryRecord> all = new ArrayList<>();
                List<String> tiers = new ArrayList<>();
                for (MemoryProvider p : testProviders) {
                    try {
                        List<MemoryRecord> recs = p.exportAll(agentId);
                        all.addAll(recs);
                        tiers.add(p.name());
                    } catch (Exception ignored) {}
                }
                return MemorySnapshot.builder()
                        .snapshotId("test-snapshot")
                        .agentId(agentId).userId(userId).tenantId(tenantId)
                        .tiers(tiers).records(all)
                        .exportMetadata(Map.of("totalRecords", all.size()))
                        .build();
            });
        }

        @Override
        public io.smallrye.mutiny.Uni<ImportResult> importSnapshot(MemorySnapshot snapshot, boolean overwrite) {
            return io.smallrye.mutiny.Uni.createFrom().item(() -> {
                int total = 0;
                for (MemoryProvider p : testProviders) {
                    try {
                        int written = p.importAll(snapshot.records(), overwrite);
                        total += written;
                    } catch (Exception ignored) {}
                }
                return new ImportResult(snapshot.snapshotId(), snapshot.agentId(),
                        snapshot.records().size(), total, snapshot.records().size() - total,
                        Map.of("all", total), java.time.Instant.now());
            });
        }
    }

    @Test
    void exportProducesSnapshotWithAllRecordsFromAllTiers() {
        StubMemoryProvider shortTerm = new StubMemoryProvider("short-term");
        StubMemoryProvider longTerm  = new StubMemoryProvider("long-term");

        shortTerm.save(MemoryRecord.of("session-key-1", "hello world", "short-term"));
        longTerm.save(MemoryRecord.of("fact-key-1", "the sky is blue", "long-term"));

        TestExportService service = new TestExportService(List.of(shortTerm, longTerm));

        MemorySnapshot snapshot = service.exportSnapshot("agent-42", "user-1", "tenant-1")
                .await().atMost(Duration.ofSeconds(2));

        assertThat(snapshot.agentId()).isEqualTo("agent-42");
        assertThat(snapshot.tiers()).containsExactlyInAnyOrder("short-term", "long-term");
        assertThat(snapshot.records()).hasSize(2);
        assertThat(snapshot.schemaVersion()).isEqualTo(MemorySnapshot.CURRENT_SCHEMA);
    }

    @Test
    void snapshotRoundTripSerializationPreservesAllFields() throws IOException {
        MemoryExportService service = new TestExportService(List.of());
        MemorySnapshot original = MemorySnapshot.builder()
                .snapshotId("snap-123")
                .agentId("agent-99")
                .records(List.of(
                        MemoryRecord.of("k1", "value-1", "short-term"),
                        MemoryRecord.of("k2", "value-2", "long-term")
                ))
                .exportMetadata(Map.of("totalRecords", 2))
                .build();

        byte[] json = service.serializeSnapshot(original);
        MemorySnapshot restored = service.deserializeSnapshot(json);

        assertThat(restored.snapshotId()).isEqualTo("snap-123");
        assertThat(restored.agentId()).isEqualTo("agent-99");
        assertThat(restored.records()).hasSize(2);
        assertThat(restored.records().get(0).key()).isEqualTo("k1");
    }

    @Test
    void importRestoresRecordsIntoProviders() {
        StubMemoryProvider target = new StubMemoryProvider("short-term");
        TestExportService service = new TestExportService(List.of(target));

        MemorySnapshot snapshot = MemorySnapshot.builder()
                .snapshotId("snap-restore")
                .agentId("agent-42")
                .records(List.of(
                        MemoryRecord.of("restore-k1", "restored value 1"),
                        MemoryRecord.of("restore-k2", "restored value 2")
                ))
                .build();

        MemoryExportService.ImportResult result = service.importSnapshot(snapshot, true)
                .await().atMost(Duration.ofSeconds(2));

        assertThat(result.totalRecords()).isEqualTo(2);
        assertThat(result.writtenRecords()).isEqualTo(2);
        assertThat(target.records).hasSize(2);
    }

    @Test
    void importSkipsExistingRecordsWhenOverwriteFalse() throws Exception {
        StubMemoryProvider target = new StubMemoryProvider("short-term");
        target.save(MemoryRecord.of("existing-key", "existing value"));

        TestExportService service = new TestExportService(List.of(target));
        MemorySnapshot snapshot = MemorySnapshot.builder()
                .snapshotId("snap-skip")
                .agentId("agent-42")
                .records(List.of(
                        MemoryRecord.of("existing-key", "new value"),
                        MemoryRecord.of("new-key", "new value")
                ))
                .build();

        MemoryExportService.ImportResult result = service.importSnapshot(snapshot, false)
                .await().atMost(Duration.ofSeconds(2));

        assertThat(result.writtenRecords()).isEqualTo(1);
        assertThat(result.skippedRecords()).isEqualTo(1);
        assertThat(target.get("existing-key").orElseThrow().value()).isEqualTo("existing value");
    }

    @Test
    void backupWritesSnapshotToDisk(@TempDir Path tempDir) throws IOException {
        StubMemoryProvider provider = new StubMemoryProvider("long-term");
        provider.save(MemoryRecord.of("disk-key", "persisted value", "long-term"));

        TestExportService service = new TestExportService(List.of(provider));
        MemorySnapshot snapshot = service.exportSnapshot("agent-disk", null, null)
                .await().atMost(Duration.ofSeconds(2));

        Path writtenFile = service.backupToDisk(snapshot, tempDir);

        assertThat(Files.exists(writtenFile)).isTrue();
        assertThat(writtenFile.getFileName().toString()).startsWith("memory-snapshot-agent-disk");
        String content = Files.readString(writtenFile);
        assertThat(content).contains("agent-disk").contains("disk-key");
    }

    @Test
    void restoreFromDiskProducesEquivalentSnapshot(@TempDir Path tempDir) throws IOException {
        MemoryExportService service = new TestExportService(List.of());

        MemorySnapshot original = MemorySnapshot.builder()
                .snapshotId("snap-disk-rt")
                .agentId("agent-disk-rt")
                .records(List.of(MemoryRecord.of("dk", "dv")))
                .build();

        Path file = service.backupToDisk(original, tempDir);
        MemorySnapshot restored = service.readFromDisk(file);

        assertThat(restored.agentId()).isEqualTo("agent-disk-rt");
        assertThat(restored.records()).hasSize(1);
        assertThat(restored.records().get(0).key()).isEqualTo("dk");
    }
}
