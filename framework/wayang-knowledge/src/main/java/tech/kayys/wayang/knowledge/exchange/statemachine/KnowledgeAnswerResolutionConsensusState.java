package tech.kayys.wayang.knowledge.exchange.statemachine;

import java.time.Instant;

public record KnowledgeAnswerResolutionConsensusState(
        String consensusId,
        String keyFingerprint,
        String resolutionFingerprint,
        String dependencyFingerprint,
        String epochId,
        KnowledgeAnswerResolutionConsensusStateStatus status,
        Instant activatedAt,
        Instant expiresAt
) {}
