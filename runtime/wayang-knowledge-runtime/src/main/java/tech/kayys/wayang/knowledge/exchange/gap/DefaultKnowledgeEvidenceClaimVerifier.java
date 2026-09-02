package tech.kayys.wayang.knowledge.exchange.gap;

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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DefaultKnowledgeEvidenceClaimVerifier
        implements KnowledgeEvidenceClaimVerifier {

    @Override
    public KnowledgeEvidenceClaimVerification verify(
            KnowledgeEvidenceClaim claim,
            KnowledgeEvidenceClaimGraph graph) {

        List<KnowledgeEvidenceClaimSupport> supports =
                graph.supports()
                        .stream()
                        .filter(s ->
                                s.claimId()
                                        .equals(claim.claimId()))
                        .sorted(
                                Comparator.comparingDouble(
                                        KnowledgeEvidenceClaimSupport
                                                ::supportScore
                                ).reversed()
                        )
                        .toList();

        List<KnowledgeEvidenceClaimContradiction>
                contradictions =
                    graph.contradictions()
                            .stream()
                            .filter(c ->
                                    c.claimId()
                                            .equals(claim.claimId()))
                            .toList();

        if (!contradictions.isEmpty()) {

            return new KnowledgeEvidenceClaimVerification(
                    claim.claimId(),
                    KnowledgeEvidenceClaimStatus.CONTRADICTED,
                    contradictions.stream()
                            .mapToDouble(
                                    KnowledgeEvidenceClaimContradiction
                                            ::confidence
                            )
                            .max()
                            .orElse(0.0),
                    supports.stream()
                            .map(
                                    KnowledgeEvidenceClaimSupport
                                            ::evidenceId
                            )
                            .toList(),
                    contradictions.stream()
                            .map(
                                    KnowledgeEvidenceClaimContradiction
                                            ::evidenceId
                            )
                            .toList(),
                    "Claim has contradictory evidence",
                    java.util.Map.of()
            );
        }

        if (supports.isEmpty()) {

            return new KnowledgeEvidenceClaimVerification(
                    claim.claimId(),
                    KnowledgeEvidenceClaimStatus.UNSUPPORTED,
                    0.0,
                    List.of(),
                    List.of(),
                    "No supporting evidence",
                    java.util.Map.of()
            );
        }

        double best =
                supports.get(0).supportScore();

        List<String> supporting =
                supports.stream()
                        .filter(s ->
                                s.supportScore() >= 0.40)
                        .map(
                                KnowledgeEvidenceClaimSupport
                                        ::evidenceId
                        )
                        .toList();

        KnowledgeEvidenceClaimStatus status;

        if (best >= 0.80) {
            status = KnowledgeEvidenceClaimStatus.SUPPORTED;
        } else if (best >= 0.40) {
            status =
                    KnowledgeEvidenceClaimStatus.PARTIALLY_SUPPORTED;
        } else {
            status =
                    KnowledgeEvidenceClaimStatus.UNSUPPORTED;
        }

        return new KnowledgeEvidenceClaimVerification(
                claim.claimId(),
                status,
                best,
                supporting,
                List.of(),
                status
                        == KnowledgeEvidenceClaimStatus.SUPPORTED
                        ? "Claim is directly supported"
                        : "Claim has insufficient direct support",
                java.util.Map.of()
        );
    }
}
