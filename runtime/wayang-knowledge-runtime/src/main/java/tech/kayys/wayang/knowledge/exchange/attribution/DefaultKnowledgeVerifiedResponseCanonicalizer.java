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


public final class DefaultKnowledgeVerifiedResponseCanonicalizer
        implements KnowledgeVerifiedResponseCanonicalizer {

    @Override
    public String canonicalize(
            KnowledgeVerifiedResponse response) {

        StringBuilder builder = new StringBuilder();

        builder.append("responseId=")
                .append(response.metadata().responseId())
                .append('\n');

        builder.append("executionId=")
                .append(response.metadata().executionId())
                .append('\n');

        builder.append("agentId=")
                .append(response.metadata().agentId())
                .append('\n');

        builder.append("status=")
                .append(response.status())
                .append('\n');

        builder.append("disposition=")
                .append(response.disposition())
                .append('\n');

        builder.append("confidence=")
                .append(response.confidence())
                .append('\n');

        response.claims()
                .stream()
                .sorted(
                        java.util.Comparator.comparing(
                                KnowledgeVerifiedClaim
                                        ::claimId
                        )
                )
                .forEach(claim -> {

                    builder.append("claim:")
                            .append(claim.claimId())
                            .append('|')
                            .append(claim.status())
                            .append('|')
                            .append(claim.confidence())
                            .append('|')
                            .append(claim.text())
                            .append('\n');
                });

        response.evidence()
                .stream()
                .sorted(
                        java.util.Comparator.comparing(
                                KnowledgeVerifiedEvidenceReference
                                        ::evidenceId
                        )
                )
                .forEach(evidence -> {

                    builder.append("evidence:")
                            .append(evidence.evidenceId())
                            .append('|')
                            .append(evidence.versionId())
                            .append('|')
                            .append(evidence.artifactId())
                            .append('\n');
                });

        return builder.toString();
    }
}
