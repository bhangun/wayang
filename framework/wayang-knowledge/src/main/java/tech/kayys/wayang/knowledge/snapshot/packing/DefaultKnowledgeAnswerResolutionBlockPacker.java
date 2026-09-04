package tech.kayys.wayang.knowledge.snapshot.packing;

import tech.kayys.wayang.knowledge.snapshot.block.KnowledgeAnswerResolutionStateBlockFingerprinter;
import tech.kayys.wayang.knowledge.snapshot.block.Sha256KnowledgeAnswerResolutionStateBlockFingerprinter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DefaultKnowledgeAnswerResolutionBlockPacker
        implements KnowledgeAnswerResolutionBlockPacker {

    private final KnowledgeAnswerResolutionBlockPackingPolicy policy;
    private final KnowledgeAnswerResolutionBlockCompressor compressor;
    private final KnowledgeAnswerResolutionCompressionPlanner planner;
    private final KnowledgeAnswerResolutionStateBlockFingerprinter fingerprinter;

    public DefaultKnowledgeAnswerResolutionBlockPacker(
            KnowledgeAnswerResolutionBlockPackingPolicy policy,
            KnowledgeAnswerResolutionBlockCompressor compressor,
            KnowledgeAnswerResolutionStateBlockFingerprinter fingerprinter) {
        this.policy = policy != null ? policy : KnowledgeAnswerResolutionBlockPackingPolicy.defaults();
        this.compressor = compressor != null ? compressor : new GzipKnowledgeAnswerResolutionBlockCompressor();
        this.planner = new KnowledgeAnswerResolutionCompressionPlanner(this.policy);
        this.fingerprinter = fingerprinter != null ? fingerprinter : new Sha256KnowledgeAnswerResolutionStateBlockFingerprinter();
    }

    public DefaultKnowledgeAnswerResolutionBlockPacker() {
        this(KnowledgeAnswerResolutionBlockPackingPolicy.defaults(),
                new GzipKnowledgeAnswerResolutionBlockCompressor(),
                new Sha256KnowledgeAnswerResolutionStateBlockFingerprinter());
    }

    @Override
    public List<KnowledgeAnswerResolutionPackedStateBlock> pack(
            List<KnowledgeAnswerResolutionLogicalStateBlock> logicalBlocks) {

        if (logicalBlocks == null || logicalBlocks.isEmpty()) {
            return List.of();
        }

        List<KnowledgeAnswerResolutionPackedStateBlock> result = new ArrayList<>();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        List<String> currentIds = new ArrayList<>();

        for (KnowledgeAnswerResolutionLogicalStateBlock block : logicalBlocks) {
            byte[] data = block.data();
            if (policy.enablePacking() && (buffer.size() + data.length <= policy.targetBlockBytes())
                    && currentIds.size() < policy.maximumPackEntries()) {
                buffer.writeBytes(data);
                currentIds.add(block.logicalId());
            } else {
                if (buffer.size() > 0) {
                    result.add(createPackedBlock(buffer.toByteArray(), currentIds));
                    buffer.reset();
                    currentIds.clear();
                }
                buffer.writeBytes(data);
                currentIds.add(block.logicalId());
            }
        }

        if (buffer.size() > 0) {
            result.add(createPackedBlock(buffer.toByteArray(), currentIds));
        }

        return result;
    }

    private KnowledgeAnswerResolutionPackedStateBlock createPackedBlock(
            byte[] rawData,
            List<String> logicalIds) {

        byte[] compressed = compressor.compress(rawData);
        KnowledgeAnswerResolutionCompressionDecision decision =
                planner.decide(rawData.length, compressed.length, compressor.algorithm());

        byte[] finalData = decision.useCompression() ? compressed : rawData;
        String finalAlgo = decision.useCompression() ? decision.algorithm() : "none";
        String blockId = "block-" + fingerprinter.fingerprint(finalData);

        return new KnowledgeAnswerResolutionPackedStateBlock(
                blockId,
                "SHA-256",
                finalAlgo,
                finalData,
                rawData.length,
                finalData.length,
                List.copyOf(logicalIds),
                Map.of("compressed", String.valueOf(decision.useCompression()))
        );
    }
}
