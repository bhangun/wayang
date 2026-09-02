package tech.kayys.wayang.knowledge.exchange.compact;

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


import java.util.Set;
import java.util.stream.Collectors;

public final class DefaultKnowledgeAnswerArtifactEquivalenceVerifier
        implements KnowledgeAnswerArtifactEquivalenceVerifier {

    @Override
    public boolean equivalent(
            KnowledgeVerifiedAnswerArtifact left,
            KnowledgeVerifiedAnswerArtifact right) {

        if (!left.tenantId()
                .equals(right.tenantId())) {

            return false;
        }

        if (left.response().status()
                != right.response().status()) {

            return false;
        }

        Set<String> leftClaims =
                left.response()
                        .claims()
                        .stream()
                        .filter(claim ->
                                claim.status()
                                        == KnowledgeEvidenceClaimStatus
                                        .SUPPORTED)
                        .map(claim ->
                                normalize(claim.text()))
                        .collect(Collectors.toSet());

        Set<String> rightClaims =
                right.response()
                        .claims()
                        .stream()
                        .filter(claim ->
                                claim.status()
                                        == KnowledgeEvidenceClaimStatus
                                        .SUPPORTED)
                        .map(claim ->
                                normalize(claim.text()))
                        .collect(Collectors.toSet());

        return leftClaims.equals(rightClaims);
    }

    private String normalize(String value) {

        return value == null
                ? ""
                : value
                    .toLowerCase()
                    .replaceAll("\\s+", " ")
                    .trim();
    }
}
