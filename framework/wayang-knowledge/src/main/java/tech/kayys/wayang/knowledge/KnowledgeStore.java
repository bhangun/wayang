package tech.kayys.wayang.knowledge;

import java.util.List;
import tech.kayys.wayang.project.ProjectContext;

/**
 * Defines operations for storing and retrieving curated project knowledge.
 */
public interface KnowledgeStore {

    /**
     * Stores a new knowledge item.
     */
    void store(ProjectContext project, KnowledgeItem item);

    /**
     * Retrieves knowledge items for a project based on a semantic query.
     */
    List<KnowledgeItem> search(ProjectContext project, String query, int limit);

    /**
     * Retrieves a specific knowledge item by ID.
     */
    KnowledgeItem get(ProjectContext project, String id);
    
    /**
     * Marks a knowledge item as superseded by a newer one.
     */
    void supersede(ProjectContext project, String oldId, String newId);

    /**
     * Deletes a knowledge item permanently.
     */
    void delete(ProjectContext project, String id);
}
