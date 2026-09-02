package tech.kayys.wayang.knowledge.exchange.attribution;

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
import java.util.List;
import java.util.UUID;

public final class DefaultKnowledgeVerifiedResponseCompiler
        implements KnowledgeVerifiedResponseCompiler {

    private final KnowledgeVerifiedResponsePolicy policy;

    public DefaultKnowledgeVerifiedResponseCompiler() {
        this(
                new DefaultKnowledgeVerifiedResponsePolicy()
        );
    }

    public DefaultKnowledgeVerifiedResponseCompiler(
            KnowledgeVerifiedResponsePolicy policy) {

        this.policy = policy;
    }

    @Override
    public KnowledgeVerifiedResponse compile(
            String candidateAnswer,
            KnowledgeEvidenceSemanticQuery query,
            KnowledgeEvidenceAnswerVerification verification) {

        KnowledgeVerifiedResponseDisposition disposition =
                policy.disposition(verification);

        KnowledgeVerifiedResponseStatus status =
                determineStatus(verification);

        List<KnowledgeVerifiedClaim> claims =
                new ArrayList<>();

        List<String> blocked =
                new ArrayList<>();

        for (KnowledgeEvidenceClaimVerification v
                : verification.claims()) {

            KnowledgeEvidenceClaim claim =
                    verification.claimGraph()
                            .claims()
                            .stream()
                            .filter(c ->
                                    c.claimId()
                                            .equals(v.claimId()))
                            .findFirst()
                            .orElse(null);

            if (claim == null) {
                continue;
            }

            KnowledgeVerifiedClaim verifiedClaim =
                    new KnowledgeVerifiedClaim(
                            claim.claimId(),
                            claim.text(),
                            claim.type(),
                            v.status(),
                            v.confidence(),
                            v.supportingEvidenceIds(),
                            v.contradictingEvidenceIds(),
                            v.reason(),
                            java.util.Map.of()
                    );

            claims.add(verifiedClaim);

            if (v.status()
                    != KnowledgeEvidenceClaimStatus.SUPPORTED) {

                blocked.add(v.claimId());
            }
        }

        String answer =
                compileAnswer(
                        candidateAnswer,
                        disposition
                );

        KnowledgeVerifiedResponseMetadata metadata =
                new KnowledgeVerifiedResponseMetadata(
                        "response-" + UUID.randomUUID(),
                        query.queryId(),
                        query.agentId(),
                        query.tenantId(),
                        query.workspaceId(),
                        query.projectId(),
                        Instant.now(),
                        "unknown",
                        "p050",
                        java.util.Map.of()
                );

        return new KnowledgeVerifiedResponse(
                metadata,
                answer,
                status,
                disposition,
                verification.confidence(),
                claims,
                List.of(),
                blocked,
                warnings(verification),
                java.util.Map.of()
        );
    }

    private KnowledgeVerifiedResponseStatus determineStatus(
            KnowledgeEvidenceAnswerVerification verification) {

        if (verification == null) {
            return KnowledgeVerifiedResponseStatus.FAILED;
        }

        return switch (verification.overallStatus()) {

            case SUPPORTED ->
                    KnowledgeVerifiedResponseStatus.VERIFIED;

            case PARTIALLY_SUPPORTED ->
                    KnowledgeVerifiedResponseStatus
                            .PARTIALLY_VERIFIED;

            case CONTRADICTED ->
                    KnowledgeVerifiedResponseStatus.CONTRADICTED;

            case UNSUPPORTED,
                 UNVERIFIABLE ->
                    KnowledgeVerifiedResponseStatus.UNVERIFIED;

            case AMBIGUOUS ->
                    KnowledgeVerifiedResponseStatus
                            .PARTIALLY_VERIFIED;

            case BLOCKED ->
                    KnowledgeVerifiedResponseStatus.BLOCKED;
        };
    }

    private String compileAnswer(
            String answer,
            KnowledgeVerifiedResponseDisposition disposition) {

        if (answer == null) {
            return "";
        }

        if (disposition
                == KnowledgeVerifiedResponseDisposition
                .RELEASE_WITH_QUALIFICATION) {

            return answer
                    + "\n\n"
                    + "Some statements could not be "
                    + "fully verified against the available evidence.";
        }

        if (disposition
                == KnowledgeVerifiedResponseDisposition.HOLD
                || disposition
                == KnowledgeVerifiedResponseDisposition.REJECT) {

            return "The requested answer could not be "
                    + "released because the available evidence "
                    + "was insufficient or contradictory.";
        }

        return answer;
    }

    private List<String> warnings(
            KnowledgeEvidenceAnswerVerification verification) {

        if (verification.releasable()) {
            return List.of();
        }

        return List.of(
                "One or more claims failed verification"
        );
    }
}
