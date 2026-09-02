package tech.kayys.wayang.knowledge.exchange.proof;

import tech.kayys.wayang.knowledge.*;
import tech.kayys.wayang.knowledge.seal.*;
import tech.kayys.wayang.knowledge.snapshot.*;
import tech.kayys.wayang.knowledge.snapshot.pack.*;
import tech.kayys.wayang.knowledge.snapshot.artifact.*;
import tech.kayys.wayang.knowledge.snapshot.merkle.*;
import tech.kayys.wayang.knowledge.exchange.*;
import tech.kayys.wayang.knowledge.exchange.auth.*;
import tech.kayys.wayang.knowledge.exchange.session.*;
import tech.kayys.wayang.knowledge.exchange.binding.*;
import tech.kayys.wayang.knowledge.exchange.envelope.*;
import tech.kayys.wayang.knowledge.exchange.trust.*;
import tech.kayys.wayang.knowledge.exchange.identity.*;
import tech.kayys.wayang.knowledge.exchange.capability.*;
import tech.kayys.wayang.knowledge.exchange.protocol.*;
import tech.kayys.wayang.knowledge.exchange.transport.*;
import tech.kayys.wayang.knowledge.exchange.framing.*;
import tech.kayys.wayang.knowledge.exchange.transfer.*;
import tech.kayys.wayang.knowledge.exchange.replication.*;
import tech.kayys.wayang.knowledge.exchange.sync.*;
import tech.kayys.wayang.knowledge.exchange.federation.*;
import tech.kayys.wayang.knowledge.exchange.routing.*;
import tech.kayys.wayang.knowledge.exchange.fusion.*;
import tech.kayys.wayang.knowledge.exchange.coverage.*;
import tech.kayys.wayang.knowledge.exchange.gap.*;
import tech.kayys.wayang.knowledge.exchange.attribution.*;
import tech.kayys.wayang.knowledge.exchange.contradiction.*;
import tech.kayys.wayang.knowledge.exchange.factuality.*;
import tech.kayys.wayang.knowledge.exchange.uncertainty.*;
import tech.kayys.wayang.knowledge.exchange.compact.*;
import tech.kayys.wayang.knowledge.exchange.resolution.*;
import tech.kayys.wayang.knowledge.exchange.quorum.*;
import tech.kayys.wayang.knowledge.exchange.selection.*;
import tech.kayys.wayang.knowledge.exchange.coordination.*;
import tech.kayys.wayang.knowledge.exchange.attestation.*;
import tech.kayys.wayang.knowledge.exchange.proof.*;
import tech.kayys.wayang.knowledge.exchange.validity.*;
import tech.kayys.wayang.knowledge.exchange.lease.*;
import tech.kayys.wayang.knowledge.exchange.recovery.*;


import java.time.Instant;
import java.util.UUID;

public final class
DefaultKnowledgeAnswerResolutionAttestationService
        implements
        KnowledgeAnswerResolutionAttestationService {

    private final
    KnowledgeAnswerResolutionAttestationCanonicalizer
            canonicalizer;

    private final
    KnowledgeAnswerResolutionAttestationSigner signer;

    public
    DefaultKnowledgeAnswerResolutionAttestationService(
            KnowledgeAnswerResolutionAttestationCanonicalizer
                    canonicalizer,
            KnowledgeAnswerResolutionAttestationSigner signer) {

        this.canonicalizer = canonicalizer;
        this.signer = signer;
    }

    @Override
    public KnowledgeAnswerResolutionAttestation attest(
            KnowledgeAnswerResolutionConsensusProposal proposal,
            KnowledgeAnswerResolutionConsensusResult result,
            String runtimeId) {

        if (result.status()
                != KnowledgeAnswerResolutionConsensusStatus
                        .CONSENSUS_REACHED) {

            throw new IllegalArgumentException(
                    "Only reached consensus may be attested"
            );
        }

        byte[] payload =
                canonicalizer.canonicalize(
                        proposal.consensusId(),
                        proposal.keyFingerprint(),
                        result.winningResolutionFingerprint(),
                        result.winningDependencyFingerprint(),
                        runtimeId,
                        signer.keyReference(),
                        signer.algorithm()
                );

        byte[] signature =
                signer.sign(payload);

        return new KnowledgeAnswerResolutionAttestation(
                UUID.randomUUID().toString(),
                proposal.consensusId(),
                proposal.keyFingerprint(),
                result.winningResolutionFingerprint(),
                result.winningDependencyFingerprint(),
                runtimeId,
                signer.keyReference(),
                signer.algorithm(),
                signature,
                Instant.now(),
                proposal.expiresAt(),
                java.util.Map.of()
        );
    }
}
