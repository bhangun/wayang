package tech.kayys.wayang.knowledge.exchange.contradiction;

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

public final class KnowledgeVerifiedAnswerArtifactFactory {

    private final KnowledgeAnswerProvenanceService
            provenanceService;

    private final KnowledgeVerifiedResponseFingerprinter
            responseFingerprinter;

    private final KnowledgeVerifiedAnswerArtifactFingerprinter
            artifactFingerprinter;

    public KnowledgeVerifiedAnswerArtifactFactory() {

        this(
                new DefaultKnowledgeAnswerProvenanceService(),
                new Sha256KnowledgeVerifiedResponseFingerprinter(),
                new Sha256KnowledgeVerifiedAnswerArtifactFingerprinter()
        );
    }

    public KnowledgeVerifiedAnswerArtifactFactory(
            KnowledgeAnswerProvenanceService provenanceService,
            KnowledgeVerifiedResponseFingerprinter responseFingerprinter,
            KnowledgeVerifiedAnswerArtifactFingerprinter
                    artifactFingerprinter) {

        this.provenanceService = provenanceService;
        this.responseFingerprinter =
                responseFingerprinter;
        this.artifactFingerprinter =
                artifactFingerprinter;
    }

    public KnowledgeVerifiedAnswerArtifact create(
            KnowledgeVerifiedResponse response,
            KnowledgeEvidenceSemanticQuery query,
            KnowledgeEvidenceAnswerVerification verification,
            String snapshotId) {

        KnowledgeAnswerProvenanceGraph provenance =
                provenanceService.build(
                        response,
                        query,
                        verification
                );

        String responseFingerprint =
                responseFingerprinter.fingerprint(
                        response
                );

        String temporaryId =
                "answer:"
                        + UUID.randomUUID();

        KnowledgeVerifiedAnswerArtifact artifact =
                new KnowledgeVerifiedAnswerArtifact(
                        temporaryId,
                        response.metadata().responseId(),
                        response.metadata().executionId(),
                        response.metadata().agentId(),
                        response.metadata().tenantId(),
                        response.metadata().workspaceId(),
                        response.metadata().projectId(),
                        response,
                        provenance,
                        snapshotId,
                        responseFingerprint,
                        Instant.now(),
                        java.util.Map.of()
                );

        String fingerprint =
                artifactFingerprinter.fingerprint(
                        artifact
                );

        return new KnowledgeVerifiedAnswerArtifact(
                fingerprint,
                artifact.responseId(),
                artifact.executionId(),
                artifact.agentId(),
                artifact.tenantId(),
                artifact.workspaceId(),
                artifact.projectId(),
                artifact.response(),
                artifact.provenance(),
                artifact.snapshotId(),
                artifact.responseFingerprint(),
                artifact.createdAt(),
                artifact.metadata()
        );
    }
}
