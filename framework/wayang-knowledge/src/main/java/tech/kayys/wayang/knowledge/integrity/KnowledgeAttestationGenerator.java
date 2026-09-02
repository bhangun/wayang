package tech.kayys.wayang.knowledge.integrity;

public interface KnowledgeAttestationGenerator {

    KnowledgeAttestation attest(KnowledgeSnapshotIntegrityResult result, String attesterId);
}
