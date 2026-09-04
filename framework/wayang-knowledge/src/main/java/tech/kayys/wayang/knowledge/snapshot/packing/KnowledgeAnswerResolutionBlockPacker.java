package tech.kayys.wayang.knowledge.snapshot.packing;

import java.util.List;

public interface KnowledgeAnswerResolutionBlockPacker {

    List<KnowledgeAnswerResolutionPackedStateBlock> pack(
            List<KnowledgeAnswerResolutionLogicalStateBlock> logicalBlocks
    );
}
