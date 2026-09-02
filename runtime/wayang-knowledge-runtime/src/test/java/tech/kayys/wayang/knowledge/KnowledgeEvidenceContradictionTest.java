package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.contradiction.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceContradictionTest {

    @Test
    public void testProvenanceGraphAndValidator() {
        DefaultKnowledgeAnswerProvenanceValidator validator =
                new DefaultKnowledgeAnswerProvenanceValidator();

        KnowledgeAnswerProvenanceNode node1 = new KnowledgeAnswerProvenanceNode(
                "node-1", KnowledgeAnswerProvenanceNodeType.EVIDENCE, "doc-1", "v1", "fp-1",
                "tenant-1", "ws-1", "proj-1", Map.of()
        );
        KnowledgeAnswerProvenanceNode node2 = new KnowledgeAnswerProvenanceNode(
                "node-2", KnowledgeAnswerProvenanceNodeType.CLAIM, "claim-1", "v1", "fp-2",
                "tenant-1", "ws-1", "proj-1", Map.of()
        );

        KnowledgeAnswerProvenanceEdge edge = new KnowledgeAnswerProvenanceEdge(
                "edge-1", "node-1", "node-2", KnowledgeAnswerProvenanceRelationType.SUPPORTS,
                "Direct reference", Instant.now(), Map.of()
        );

        KnowledgeAnswerProvenanceGraph graph = new KnowledgeAnswerProvenanceGraph(
                "graph-1", "resp-1", List.of(node1, node2), List.of(edge), Map.of()
        );

        KnowledgeAnswerProvenanceValidationResult validation = validator.validate(graph);
        assertNotNull(validation);
        assertTrue(validation.valid());
    }
}
