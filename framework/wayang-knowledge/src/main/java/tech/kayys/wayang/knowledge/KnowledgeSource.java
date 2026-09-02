package tech.kayys.wayang.knowledge;

import java.util.concurrent.CompletionStage;

/**
 * Provider of knowledge to the Wayang runtime.
 */
public interface KnowledgeSource {

    String id();

    default String name() {
        return id();
    }

    default KnowledgeSourceDescriptor descriptor() {
        return new KnowledgeSourceDescriptor(id(), name(), "generic", true);
    }

    CompletionStage<KnowledgeResult> query(KnowledgeQuery query, KnowledgeContext context);

    default boolean isHealthy() {
        return true;
    }
}
