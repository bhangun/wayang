package tech.kayys.wayang.knowledge;

import java.util.List;

/**
 * Compresses selected evidence units into a concise format suitable for SLM context windows.
 */
public interface KnowledgeCompressor {

    List<KnowledgeEvidence> compress(
            List<KnowledgeEvidence> evidence,
            KnowledgeBudget budget
    );
}
