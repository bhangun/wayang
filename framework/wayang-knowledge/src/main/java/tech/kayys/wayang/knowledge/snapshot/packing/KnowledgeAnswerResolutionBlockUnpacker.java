package tech.kayys.wayang.knowledge.snapshot.packing;

public interface KnowledgeAnswerResolutionBlockUnpacker {

    byte[] unpack(KnowledgeAnswerResolutionPackedStateBlock packedBlock);
}
