package tech.kayys.wayang.knowledge.snapshot;

public interface KnowledgeVersionProvider {

    KnowledgeVersionReference resolve(String id, String versionId);
}
