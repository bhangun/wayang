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


import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class DefaultKnowledgeEvidenceClaimMatcher
        implements KnowledgeEvidenceClaimMatcher {

    @Override
    public KnowledgeEvidenceClaimSupport match(
            KnowledgeEvidenceClaim claim,
            KnowledgeEvidenceFusionCandidate candidate) {

        String claimText =
                normalize(claim.text());

        String evidenceText =
                normalize(
                        candidate.evidence().excerpt()
                );

        Set<String> claimTokens =
                tokens(claimText);

        Set<String> evidenceTokens =
                tokens(evidenceText);

        if (claimTokens.isEmpty()
                || evidenceTokens.isEmpty()) {

            return new KnowledgeEvidenceClaimSupport(
                    claim.claimId(),
                    candidate.evidence().knowledgeId(),
                    0.0,
                    false,
                    candidate.authorityScore() >= 0.80,
                    candidate.trustScore() >= 0.80,
                    "No lexical evidence match",
                    java.util.Map.of()
            );
        }

        Set<String> intersection =
                new HashSet<>(claimTokens);

        intersection.retainAll(evidenceTokens);

        double lexicalScore =
                (double) intersection.size()
                        / claimTokens.size();

        double score =
                Math.min(
                        1.0,
                        lexicalScore * 0.60
                                + candidate.finalScore() * 0.40
                );

        boolean direct = score >= 0.80;

        return new KnowledgeEvidenceClaimSupport(
                claim.claimId(),
                candidate.evidence().knowledgeId(),
                score,
                direct,
                candidate.authorityScore() >= 0.80,
                candidate.trustScore() >= 0.80,
                direct
                        ? "Evidence directly matches claim"
                        : "Evidence partially matches claim",
                java.util.Map.of()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ");
    }

    private Set<String> tokens(String value) {
        Set<String> result = new HashSet<>();

        for (String token : value.split("\\s+")) {
            if (token.length() >= 3) {
                result.add(token);
            }
        }

        return result;
    }
}
