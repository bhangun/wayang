package tech.kayys.wayang.knowledge.exchange.statemachine;

import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionLogEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DefaultKnowledgeAnswerResolutionStateMachine
        implements KnowledgeAnswerResolutionStateMachine {

    private final KnowledgeAnswerResolutionStateFingerprinter fingerprinter;
    private KnowledgeAnswerResolutionState currentState = KnowledgeAnswerResolutionState.initial();

    public DefaultKnowledgeAnswerResolutionStateMachine(
            KnowledgeAnswerResolutionStateFingerprinter fingerprinter) {
        this.fingerprinter = Objects.requireNonNull(fingerprinter, "fingerprinter");
    }

    public DefaultKnowledgeAnswerResolutionStateMachine() {
        this(new DefaultKnowledgeAnswerResolutionStateFingerprinter());
    }

    @Override
    public synchronized void apply(KnowledgeAnswerResolutionLogEntry entry) {
        Objects.requireNonNull(entry, "entry");

        Map<String, KnowledgeAnswerResolutionConsensusState> consensuses =
                new HashMap<>(currentState.consensuses());
        Set<String> revoked = new HashSet<>(currentState.revokedConsensusIds());
        Set<String> leases = new HashSet<>(currentState.activeLeaseIds());
        Set<String> runtimes = new HashSet<>(currentState.activeRuntimeIds());
        Map<String, byte[]> entries = new HashMap<>(currentState.entries());

        switch (entry.type()) {
            case PROPOSAL_CREATED -> {
                if (entry.consensusId() != null) {
                    consensuses.put(entry.consensusId(), new KnowledgeAnswerResolutionConsensusState(
                            entry.consensusId(),
                            entry.keyFingerprint(),
                            entry.payloadFingerprint(),
                            null,
                            entry.epochId(),
                            KnowledgeAnswerResolutionConsensusStateStatus.PROPOSED,
                            entry.createdAt(),
                            null
                    ));
                }
            }
            case CONSENSUS_ACTIVATED -> {
                if (entry.consensusId() != null) {
                    KnowledgeAnswerResolutionConsensusState prev = consensuses.get(entry.consensusId());
                    consensuses.put(entry.consensusId(), new KnowledgeAnswerResolutionConsensusState(
                            entry.consensusId(),
                            entry.keyFingerprint(),
                            prev != null ? prev.resolutionFingerprint() : entry.payloadFingerprint(),
                            prev != null ? prev.dependencyFingerprint() : null,
                            entry.epochId(),
                            KnowledgeAnswerResolutionConsensusStateStatus.ACTIVE,
                            entry.createdAt(),
                            null
                    ));
                }
            }
            case CONSENSUS_REVOKED -> {
                if (entry.consensusId() != null) {
                    revoked.add(entry.consensusId());
                    KnowledgeAnswerResolutionConsensusState prev = consensuses.get(entry.consensusId());
                    if (prev != null) {
                        consensuses.put(entry.consensusId(), new KnowledgeAnswerResolutionConsensusState(
                                prev.consensusId(),
                                prev.keyFingerprint(),
                                prev.resolutionFingerprint(),
                                prev.dependencyFingerprint(),
                                prev.epochId(),
                                KnowledgeAnswerResolutionConsensusStateStatus.REVOKED,
                                prev.activatedAt(),
                                entry.createdAt()
                        ));
                    }
                }
            }
            case LEASE_ISSUED, LEASE_RENEWED -> {
                if (entry.consensusId() != null) {
                    leases.add(entry.consensusId());
                }
            }
            case LEASE_EXPIRED -> {
                if (entry.consensusId() != null) {
                    leases.remove(entry.consensusId());
                }
            }
            case MEMBERSHIP_TRANSITION_FINALIZED -> {
                if (entry.runtimeId() != null) {
                    runtimes.add(entry.runtimeId());
                }
            }
            default -> {
                // Other entries update term/index
            }
        }

        if (entry.runtimeId() != null && !runtimes.contains(entry.runtimeId())) {
            runtimes.add(entry.runtimeId());
        }

        currentState = new KnowledgeAnswerResolutionState(
                entry.index(),
                entry.term(),
                entry.epochId(),
                consensuses,
                revoked,
                leases,
                runtimes,
                entries
        );
    }

    @Override
    public synchronized KnowledgeAnswerResolutionState state() {
        return currentState;
    }

    @Override
    public synchronized String stateFingerprint() {
        return fingerprinter.fingerprint(currentState);
    }

    @Override
    public synchronized void reset() {
        this.currentState = KnowledgeAnswerResolutionState.initial();
    }
}
