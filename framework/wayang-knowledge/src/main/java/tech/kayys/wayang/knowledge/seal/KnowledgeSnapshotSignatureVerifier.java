package tech.kayys.wayang.knowledge.seal;

public interface KnowledgeSnapshotSignatureVerifier {

    boolean supports(KnowledgeSnapshotSealAlgorithm algorithm);

    boolean verify(byte[] payload, byte[] signature, String keyId, String keyVersion);
}
