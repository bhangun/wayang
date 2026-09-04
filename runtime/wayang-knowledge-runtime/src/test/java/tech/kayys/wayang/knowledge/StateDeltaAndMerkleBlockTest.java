package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionState;
import tech.kayys.wayang.knowledge.snapshot.block.InMemoryKnowledgeAnswerResolutionStateBlockStore;
import tech.kayys.wayang.knowledge.snapshot.block.KnowledgeAnswerResolutionStateBlock;
import tech.kayys.wayang.knowledge.snapshot.block.KnowledgeAnswerResolutionStateBlockFactory;
import tech.kayys.wayang.knowledge.snapshot.block.KnowledgeAnswerResolutionStateBlockStore;
import tech.kayys.wayang.knowledge.snapshot.block.KnowledgeAnswerResolutionStateMerkleDag;
import tech.kayys.wayang.knowledge.snapshot.block.KnowledgeAnswerResolutionStateMerkleDagBuilder;
import tech.kayys.wayang.knowledge.snapshot.block.Sha256KnowledgeAnswerResolutionStateBlockFingerprinter;
import tech.kayys.wayang.knowledge.snapshot.delta.DefaultKnowledgeAnswerResolutionStateDeltaApplier;
import tech.kayys.wayang.knowledge.snapshot.delta.DefaultKnowledgeAnswerResolutionStateDeltaBuilder;
import tech.kayys.wayang.knowledge.snapshot.delta.DefaultKnowledgeAnswerResolutionStateDeltaValidator;
import tech.kayys.wayang.knowledge.snapshot.delta.KnowledgeAnswerResolutionStateDelta;
import tech.kayys.wayang.knowledge.snapshot.delta.KnowledgeAnswerResolutionStateDeltaOperation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StateDeltaAndMerkleBlockTest {

    @Test
    @DisplayName("P071: Test State Delta Builder, Applier, and Validator")
    void testStateDelta() {
        Map<String, byte[]> beforeEntries = Map.of(
                "key1", "val1".getBytes(),
                "key2", "val2".getBytes()
        );

        Map<String, byte[]> afterEntries = Map.of(
                "key2", "val2-updated".getBytes(),
                "key3", "val3-new".getBytes()
        );

        KnowledgeAnswerResolutionState source = new KnowledgeAnswerResolutionState(
                10L, 1L, "epoch-1", Map.of(), Set.of(), Set.of(), Set.of(), beforeEntries
        );

        KnowledgeAnswerResolutionState target = new KnowledgeAnswerResolutionState(
                12L, 1L, "epoch-1", Map.of(), Set.of(), Set.of(), Set.of(), afterEntries
        );

        var deltaBuilder = new DefaultKnowledgeAnswerResolutionStateDeltaBuilder();
        KnowledgeAnswerResolutionStateDelta delta = deltaBuilder.build(
                source, target, "snap-1", "snap-2", "t1", "epoch-1", "epoch-1"
        );

        assertEquals(3, delta.operations().size());

        var deltaValidator = new DefaultKnowledgeAnswerResolutionStateDeltaValidator();
        assertTrue(deltaValidator.validate(source, delta));

        var deltaApplier = new DefaultKnowledgeAnswerResolutionStateDeltaApplier();
        KnowledgeAnswerResolutionState reconstructed = deltaApplier.apply(source, delta);

        assertEquals(12L, reconstructed.lastAppliedIndex());
        assertFalse(reconstructed.entries().containsKey("key1"));
        assertArrayEquals("val2-updated".getBytes(), reconstructed.entries().get("key2"));
        assertArrayEquals("val3-new".getBytes(), reconstructed.entries().get("key3"));
    }

    @Test
    @DisplayName("P072: Test Content-Addressed State Blocks & Merkle DAG Builder")
    void testStateBlockAndMerkleDag() {
        var fingerprinter = new Sha256KnowledgeAnswerResolutionStateBlockFingerprinter();
        var factory = new KnowledgeAnswerResolutionStateBlockFactory(fingerprinter);

        KnowledgeAnswerResolutionStateBlock b1 = factory.create("block content 1".getBytes(), Map.of());
        KnowledgeAnswerResolutionStateBlock b2 = factory.create("block content 2".getBytes(), Map.of());
        KnowledgeAnswerResolutionStateBlock b3 = factory.create("block content 3".getBytes(), Map.of());

        assertTrue(b1.blockId().startsWith("sha256:"));
        assertEquals(b1.sizeBytes(), "block content 1".getBytes().length);

        KnowledgeAnswerResolutionStateBlockStore store = new InMemoryKnowledgeAnswerResolutionStateBlockStore();
        store.put(b1);
        store.put(b2);

        assertTrue(store.contains(b1.blockId()));
        assertEquals(b1, store.get(b1.blockId()).orElse(null));

        var dagBuilder = new KnowledgeAnswerResolutionStateMerkleDagBuilder();
        KnowledgeAnswerResolutionStateMerkleDag dag = dagBuilder.build(List.of(b1, b2, b3));

        assertNotNull(dag.rootHash());
        assertFalse(dag.rootHash().isBlank());
        assertEquals(6, dag.nodes().size()); // 3 leaves + 2 level-1 nodes + 1 root node
    }
}
