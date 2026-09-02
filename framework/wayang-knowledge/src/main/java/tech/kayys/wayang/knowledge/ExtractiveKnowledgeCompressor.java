package tech.kayys.wayang.knowledge;

import java.util.ArrayList;
import java.util.List;

/**
 * Extractive evidence compressor that trims content to fit budget while strictly preserving provenance.
 */
public class ExtractiveKnowledgeCompressor implements KnowledgeCompressor {

    private final int maxContentLength;

    public ExtractiveKnowledgeCompressor(int maxContentLength) {
        this.maxContentLength = maxContentLength > 0 ? maxContentLength : 250;
    }

    public ExtractiveKnowledgeCompressor() {
        this(250);
    }

    @Override
    public List<KnowledgeEvidence> compress(
            List<KnowledgeEvidence> evidence,
            KnowledgeBudget budget
    ) {
        if (evidence == null || evidence.isEmpty()) {
            return List.of();
        }

        List<KnowledgeEvidence> compressed = new ArrayList<>();

        for (KnowledgeEvidence ev : evidence) {
            String content = ev.item().content();
            if (content.length() > maxContentLength) {
                content = content.substring(0, maxContentLength).trim() + "...";
            }

            KnowledgeItem compressedItem = new KnowledgeItem(
                    ev.item().id(),
                    ev.item().sourceId(),
                    ev.item().type(),
                    ev.item().title(),
                    content,
                    ev.item().metadata(),
                    ev.item().provenance(),
                    ev.item().authority(),
                    ev.item().validity(),
                    ev.item().classification(),
                    ev.item().sensitivity(),
                    ev.item().trustLevel(),
                    ev.item().revision()
            );

            compressed.add(new KnowledgeEvidence(
                    compressedItem,
                    ev.score(),
                    ev.retrievalMethod() + ":compressed",
                    ev.metadata()
            ));
        }

        return compressed;
    }
}
