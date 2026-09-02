package tech.kayys.wayang.knowledge.exchange.factuality;

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
import java.util.ArrayList;

public final class DefaultKnowledgeVerifiedAnswerArtifactVerifier
        implements KnowledgeVerifiedAnswerArtifactVerifier {

    private final KnowledgeVerifiedAnswerArtifactFingerprinter
            fingerprinter;

    private final KnowledgeAnswerProvenanceValidator
            provenanceValidator;

    public DefaultKnowledgeVerifiedAnswerArtifactVerifier() {

        this(
                new Sha256KnowledgeVerifiedAnswerArtifactFingerprinter(),
                new DefaultKnowledgeAnswerProvenanceValidator()
        );
    }

    public DefaultKnowledgeVerifiedAnswerArtifactVerifier(
            KnowledgeVerifiedAnswerArtifactFingerprinter fingerprinter,
            KnowledgeAnswerProvenanceValidator provenanceValidator) {

        this.fingerprinter = fingerprinter;
        this.provenanceValidator = provenanceValidator;
    }

    @Override
    public KnowledgeVerifiedAnswerExchangeVerificationResult verify(
            KnowledgeVerifiedAnswerArtifact artifact,
            KnowledgeVerifiedAnswerExchangeRequest request) {

        var issues = new ArrayList<
                KnowledgeVerifiedAnswerVerificationIssue>();

        if (artifact == null) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "MISSING_ARTIFACT",
                            "Answer artifact is missing",
                            true
                    )
            );

            return result(
                    KnowledgeVerifiedAnswerExchangeVerificationStatus
                            .FAILED,
                    null,
                    null,
                    null,
                    false,
                    false,
                    false,
                    false,
                    false,
                    issues
            );
        }

        if (request.expiredAt(Instant.now())) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "REQUEST_EXPIRED",
                            "Exchange request has expired",
                            true
                    )
            );
        }

        if (!artifact.artifactId()
                .equals(request.artifactId())) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "ARTIFACT_ID_MISMATCH",
                            "Artifact identity does not match request",
                            true
                    )
            );
        }

        String fingerprint =
                fingerprinter.fingerprint(artifact);

        boolean artifactIntegrity =
                fingerprint.equals(
                        artifact.artifactId()
                );

        if (!artifactIntegrity) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "ARTIFACT_FINGERPRINT_MISMATCH",
                            "Artifact fingerprint mismatch",
                            true
                    )
            );
        }

        var provenance =
                provenanceValidator.validate(
                        artifact.provenance()
                );

        boolean provenanceValid =
                provenance.valid();

        if (!provenanceValid) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "PROVENANCE_INVALID",
                            "Artifact provenance graph is invalid",
                            true
                    )
            );
        }

        if (request.requireSnapshot()
                && (artifact.snapshotId() == null
                || artifact.snapshotId().isBlank())) {

            issues.add(
                    new KnowledgeVerifiedAnswerVerificationIssue(
                            "SNAPSHOT_REQUIRED",
                            "A decision snapshot is required",
                            true
                    )
            );
        }

        boolean accepted =
                issues.stream()
                        .noneMatch(
                                KnowledgeVerifiedAnswerVerificationIssue
                                        ::blocking
                        );

        return result(
                accepted
                        ? KnowledgeVerifiedAnswerExchangeVerificationStatus
                                .VERIFIED
                        : KnowledgeVerifiedAnswerExchangeVerificationStatus
                                .INTEGRITY_FAILED,
                artifact.artifactId(),
                artifact.responseId(),
                artifact.snapshotId(),
                artifactIntegrity,
                provenanceValid,
                artifact.snapshotId() != null,
                artifactIntegrity,
                false,
                issues
        );
    }

    private KnowledgeVerifiedAnswerExchangeVerificationResult result(
            KnowledgeVerifiedAnswerExchangeVerificationStatus status,
            String artifactId,
            String responseId,
            String snapshotId,
            boolean authorization,
            boolean provenance,
            boolean snapshot,
            boolean integrity,
            boolean seal,
            java.util.List<
                    KnowledgeVerifiedAnswerVerificationIssue> issues) {

        return new KnowledgeVerifiedAnswerExchangeVerificationResult(
                status,
                artifactId,
                responseId,
                snapshotId,
                artifactId,
                authorization,
                provenance,
                snapshot,
                integrity,
                seal,
                issues,
                java.util.Map.of()
        );
    }
}
