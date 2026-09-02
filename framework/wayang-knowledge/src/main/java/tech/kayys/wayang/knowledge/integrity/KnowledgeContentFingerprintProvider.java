package tech.kayys.wayang.knowledge.integrity;

public interface KnowledgeContentFingerprintProvider {

    String fingerprint(String knowledgeId, String versionId);
}
