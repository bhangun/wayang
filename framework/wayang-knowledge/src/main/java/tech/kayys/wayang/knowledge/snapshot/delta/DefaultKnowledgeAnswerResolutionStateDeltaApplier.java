package tech.kayys.wayang.knowledge.snapshot.delta;

import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionState;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DefaultKnowledgeAnswerResolutionStateDeltaApplier
        implements KnowledgeAnswerResolutionStateDeltaApplier {

    @Override
    public KnowledgeAnswerResolutionState apply(
            KnowledgeAnswerResolutionState source,
            KnowledgeAnswerResolutionStateDelta delta) {

        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(delta, "delta");

        Map<String, byte[]> entries = new HashMap<>(source.entries());

        for (KnowledgeAnswerResolutionStateDeltaOperation op : delta.operations()) {
            switch (op.type()) {
                case PUT, REPLACE -> {
                    if (op.value() != null) {
                        entries.put(op.key(), op.value());
                    }
                }
                case REMOVE -> entries.remove(op.key());
            }
        }

        return new KnowledgeAnswerResolutionState(
                delta.targetIndex(),
                source.currentTerm(),
                delta.targetEpochId(),
                source.consensuses(),
                source.revokedConsensusIds(),
                source.activeLeaseIds(),
                source.activeRuntimeIds(),
                entries
        );
    }
}
