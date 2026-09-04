package tech.kayys.wayang.knowledge.snapshot.delta;

import tech.kayys.wayang.knowledge.exchange.statemachine.DefaultKnowledgeAnswerResolutionStateFingerprinter;
import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionState;
import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionStateFingerprinter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public final class DefaultKnowledgeAnswerResolutionStateDeltaBuilder
        implements KnowledgeAnswerResolutionStateDeltaBuilder {

    private final KnowledgeAnswerResolutionStateFingerprinter fingerprinter;

    public DefaultKnowledgeAnswerResolutionStateDeltaBuilder(
            KnowledgeAnswerResolutionStateFingerprinter fingerprinter) {
        this.fingerprinter = fingerprinter != null
                ? fingerprinter
                : new DefaultKnowledgeAnswerResolutionStateFingerprinter();
    }

    public DefaultKnowledgeAnswerResolutionStateDeltaBuilder() {
        this(new DefaultKnowledgeAnswerResolutionStateFingerprinter());
    }

    @Override
    public KnowledgeAnswerResolutionStateDelta build(
            KnowledgeAnswerResolutionState source,
            KnowledgeAnswerResolutionState target,
            String sourceSnapshotId,
            String targetSnapshotId,
            String tenantId,
            String sourceEpochId,
            String targetEpochId) {

        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");

        Map<String, byte[]> before = source.entries();
        Map<String, byte[]> after = target.entries();

        List<KnowledgeAnswerResolutionStateDeltaOperation> operations = new ArrayList<>();

        Set<String> keys = new TreeSet<>();
        keys.addAll(before.keySet());
        keys.addAll(after.keySet());

        for (String key : keys) {
            byte[] oldValue = before.get(key);
            byte[] newValue = after.get(key);

            if (oldValue == null && newValue != null) {
                operations.add(new KnowledgeAnswerResolutionStateDeltaOperation(
                        KnowledgeAnswerResolutionStateDeltaOperation.Type.PUT,
                        key,
                        null,
                        fingerprint(newValue),
                        newValue,
                        Map.of()
                ));
            } else if (oldValue != null && newValue == null) {
                operations.add(new KnowledgeAnswerResolutionStateDeltaOperation(
                        KnowledgeAnswerResolutionStateDeltaOperation.Type.REMOVE,
                        key,
                        fingerprint(oldValue),
                        null,
                        null,
                        Map.of()
                ));
            } else if (!Arrays.equals(oldValue, newValue)) {
                operations.add(new KnowledgeAnswerResolutionStateDeltaOperation(
                        KnowledgeAnswerResolutionStateDeltaOperation.Type.REPLACE,
                        key,
                        fingerprint(oldValue),
                        fingerprint(newValue),
                        newValue,
                        Map.of()
                ));
            }
        }

        String sourceFingerprint = fingerprinter.fingerprint(source);
        String targetFingerprint = fingerprinter.fingerprint(target);
        String deltaFingerprint = fingerprintOperations(operations);

        return new KnowledgeAnswerResolutionStateDelta(
                UUID.randomUUID().toString(),
                sourceSnapshotId,
                targetSnapshotId,
                tenantId,
                sourceEpochId,
                targetEpochId,
                source.lastAppliedIndex(),
                target.lastAppliedIndex(),
                sourceFingerprint,
                targetFingerprint,
                deltaFingerprint,
                operations,
                estimateStateSize(target),
                estimateDeltaSize(operations),
                Map.of()
        );
    }

    private long estimateStateSize(KnowledgeAnswerResolutionState state) {
        return state.entries().values().stream()
                .mapToLong(value -> value == null ? 0 : value.length)
                .sum();
    }

    private long estimateDeltaSize(List<KnowledgeAnswerResolutionStateDeltaOperation> operations) {
        long size = 0;
        for (var op : operations) {
            size += op.key().getBytes(StandardCharsets.UTF_8).length;
            if (op.value() != null) {
                size += op.value().length;
            }
            size += 128;
        }
        return size;
    }

    private String fingerprint(byte[] value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private String fingerprintOperations(List<KnowledgeAnswerResolutionStateDeltaOperation> operations) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (var op : operations) {
                digest.update(op.type().name().getBytes(StandardCharsets.UTF_8));
                digest.update(op.key().getBytes(StandardCharsets.UTF_8));
                if (op.value() != null) {
                    digest.update(op.value());
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
