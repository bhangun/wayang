package tech.kayys.wayang.knowledge.seal;

public interface KnowledgeSnapshotSigner {

    KnowledgeSnapshotSealAlgorithm algorithm();

    String keyId();

    String keyVersion();

    byte[] sign(byte[] payload);
}
