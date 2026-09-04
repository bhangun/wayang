package tech.kayys.wayang.knowledge.snapshot.block;

public interface KnowledgeAnswerResolutionStateBlockFingerprinter {
    String fingerprint(byte[] data);
}
