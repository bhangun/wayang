package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.snapshot.cache.InMemoryKnowledgeAnswerResolutionGlobalSnapshotIndex;
import tech.kayys.wayang.knowledge.snapshot.cache.InMemoryKnowledgeAnswerResolutionSharedStateBlockStore;
import tech.kayys.wayang.knowledge.snapshot.cache.KnowledgeAnswerResolutionGlobalSnapshotIndex;
import tech.kayys.wayang.knowledge.snapshot.cache.KnowledgeAnswerResolutionSharedStateBlock;
import tech.kayys.wayang.knowledge.snapshot.cache.KnowledgeAnswerResolutionSharedStateBlockStore;
import tech.kayys.wayang.knowledge.snapshot.cache.KnowledgeAnswerResolutionSnapshotBlockReference;
import tech.kayys.wayang.knowledge.snapshot.cache.KnowledgeAnswerResolutionStateBlockDeduplicationService;
import tech.kayys.wayang.knowledge.snapshot.cache.KnowledgeAnswerResolutionStateBlockId;
import tech.kayys.wayang.knowledge.snapshot.packing.DefaultKnowledgeAnswerResolutionBlockPacker;
import tech.kayys.wayang.knowledge.snapshot.packing.DefaultKnowledgeAnswerResolutionBlockUnpacker;
import tech.kayys.wayang.knowledge.snapshot.packing.GzipKnowledgeAnswerResolutionBlockCompressor;
import tech.kayys.wayang.knowledge.snapshot.packing.KnowledgeAnswerResolutionBlockPackingPolicy;
import tech.kayys.wayang.knowledge.snapshot.packing.KnowledgeAnswerResolutionLogicalStateBlock;
import tech.kayys.wayang.knowledge.snapshot.packing.KnowledgeAnswerResolutionPackedStateBlock;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeduplicationAndPackingTest {

    @Test
    @DisplayName("P073: Test Cross-Runtime Deduplication Store and Global Snapshot Index")
    void testDeduplicationAndIndex() {
        KnowledgeAnswerResolutionSharedStateBlockStore store =
                new InMemoryKnowledgeAnswerResolutionSharedStateBlockStore();
        var dedupService = new KnowledgeAnswerResolutionStateBlockDeduplicationService(store);

        byte[] payload = "shared state block data across multiple runtimes".getBytes();
        KnowledgeAnswerResolutionSharedStateBlock b1 = dedupService.put(payload, Map.of());
        KnowledgeAnswerResolutionSharedStateBlock b2 = dedupService.put(payload, Map.of());

        assertEquals(b1.id(), b2.id());
        assertTrue(store.contains(b1.id()));

        KnowledgeAnswerResolutionGlobalSnapshotIndex index =
                new InMemoryKnowledgeAnswerResolutionGlobalSnapshotIndex();

        KnowledgeAnswerResolutionSnapshotBlockReference ref1 =
                new KnowledgeAnswerResolutionSnapshotBlockReference(
                        "snap-A", "tenant-1", "ws-1", "proj-1", b1.id(), 0L, System.currentTimeMillis()
                );
        KnowledgeAnswerResolutionSnapshotBlockReference ref2 =
                new KnowledgeAnswerResolutionSnapshotBlockReference(
                        "snap-B", "tenant-2", "ws-2", "proj-2", b1.id(), 0L, System.currentTimeMillis()
                );

        index.index(ref1);
        index.index(ref2);

        assertEquals(1, index.findBySnapshot("snap-A").size());
        assertEquals(2, index.findByBlock(b1.id()).size());

        index.removeSnapshot("snap-A");
        assertEquals(0, index.findBySnapshot("snap-A").size());
        assertEquals(1, index.findByBlock(b1.id()).size());
    }

    @Test
    @DisplayName("P074: Test State Block Packing, Compression, and Unpacking")
    void testBlockPackingAndUnpacking() {
        var compressor = new GzipKnowledgeAnswerResolutionBlockCompressor();
        var policy = new KnowledgeAnswerResolutionBlockPackingPolicy(
                1024, 64, 4096, 100, true, true, true, 0.05
        );

        var packer = new DefaultKnowledgeAnswerResolutionBlockPacker(policy, compressor, null);
        var unpacker = new DefaultKnowledgeAnswerResolutionBlockUnpacker(compressor);

        // Highly compressible payload
        byte[] payload1 = "AAAAABBBBBCCCCCDDDDDEEEEEFFFFFGGGGGHHHHHIIIIIJJJJJKKKKKLLLLLMMMMMNNNNNOOOOO".repeat(10).getBytes();
        byte[] payload2 = "11111222223333344444555556666677777888889999900000".repeat(10).getBytes();

        KnowledgeAnswerResolutionLogicalStateBlock lb1 =
                new KnowledgeAnswerResolutionLogicalStateBlock("log-1", payload1, Map.of());
        KnowledgeAnswerResolutionLogicalStateBlock lb2 =
                new KnowledgeAnswerResolutionLogicalStateBlock("log-2", payload2, Map.of());

        List<KnowledgeAnswerResolutionPackedStateBlock> packed = packer.pack(List.of(lb1, lb2));
        assertFalse(packed.isEmpty());

        KnowledgeAnswerResolutionPackedStateBlock p0 = packed.get(0);
        assertNotNull(p0.blockId());
        assertTrue(p0.logicalBlockIds().contains("log-1"));

        byte[] unpacked = unpacker.unpack(p0);
        assertNotNull(unpacked);
        assertTrue(unpacked.length > 0);
    }
}
