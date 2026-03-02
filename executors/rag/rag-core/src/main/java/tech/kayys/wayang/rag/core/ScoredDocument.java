package tech.kayys.wayang.rag.core;

import dev.langchain4j.data.segment.TextSegment;

public record ScoredDocument(TextSegment segment, double score)
        implements Comparable<ScoredDocument> {
    @Override
    public int compareTo(ScoredDocument other) {
        return Double.compare(other.score, this.score);
    }
}