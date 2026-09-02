package tech.kayys.wayang.knowledge;

import java.util.List;
import java.util.Optional;

/**
 * Registry for discovering and managing knowledge sources.
 */
public interface KnowledgeRegistry {

    void register(KnowledgeSource source);

    void unregister(String sourceId);

    Optional<KnowledgeSource> find(String sourceId);

    List<KnowledgeSource> sources();

    default List<KnowledgeSource> resolve(KnowledgeContext context) {
        if (context == null || context.sourceIds().isEmpty()) {
            return sources();
        }

        return context.sourceIds().stream()
                .map(this::find)
                .flatMap(Optional::stream)
                .toList();
    }
}
