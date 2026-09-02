package tech.kayys.wayang.knowledge.seal;

public interface KnowledgeSnapshotSealCanonicalizer {

    byte[] canonicalize(KnowledgeSnapshotSealPayload payload);
}
