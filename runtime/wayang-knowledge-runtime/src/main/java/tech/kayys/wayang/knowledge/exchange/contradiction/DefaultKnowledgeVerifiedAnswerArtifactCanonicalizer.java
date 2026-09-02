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


public final class DefaultKnowledgeVerifiedAnswerArtifactCanonicalizer
        implements KnowledgeVerifiedAnswerArtifactCanonicalizer {

    @Override
    public String canonicalize(
            KnowledgeVerifiedAnswerArtifact artifact) {

        StringBuilder b = new StringBuilder();

        b.append("artifactId=")
                .append(artifact.artifactId())
                .append('\n');

        b.append("responseId=")
                .append(artifact.responseId())
                .append('\n');

        b.append("executionId=")
                .append(artifact.executionId())
                .append('\n');

        b.append("agentId=")
                .append(artifact.agentId())
                .append('\n');

        b.append("tenantId=")
                .append(artifact.tenantId())
                .append('\n');

        b.append("workspaceId=")
                .append(artifact.workspaceId())
                .append('\n');

        b.append("projectId=")
                .append(artifact.projectId())
                .append('\n');

        b.append("responseFingerprint=")
                .append(artifact.responseFingerprint())
                .append('\n');

        b.append("snapshotId=")
                .append(artifact.snapshotId())
                .append('\n');

        b.append("responseStatus=")
                .append(artifact.response().status())
                .append('\n');

        b.append("disposition=")
                .append(artifact.response().disposition())
                .append('\n');

        artifact.provenance()
                .nodes()
                .stream()
                .sorted(
                        java.util.Comparator.comparing(
                                KnowledgeAnswerProvenanceNode
                                        ::nodeId
                        )
                )
                .forEach(node ->
                        b.append("node:")
                                .append(node.nodeId())
                                .append('|')
                                .append(node.type())
                                .append('|')
                                .append(node.externalId())
                                .append('|')
                                .append(node.fingerprint())
                                .append('\n')
                );

        artifact.provenance()
                .edges()
                .stream()
                .sorted(
                        java.util.Comparator.comparing(
                                KnowledgeAnswerProvenanceEdge
                                        ::edgeId
                        )
                )
                .forEach(edge ->
                        b.append("edge:")
                                .append(edge.edgeId())
                                .append('|')
                                .append(edge.sourceNodeId())
                                .append('|')
                                .append(edge.targetNodeId())
                                .append('|')
                                .append(edge.relation())
                                .append('\n')
                );

        return b.toString();
    }
}
