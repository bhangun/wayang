package tech.kayys.wayang.rag.domain;

/**
 * Represents a citation for a piece of information generated in a RAG response.
 */
public class Citation {
    private final int index;
    private final String content;
    private final String sourceUri;
    private final String title;
    private final int pageNumber;
    private final String sectionTitle;
    private final float confidenceScore;

    public Citation(int index, String content, String sourceUri, String title, int pageNumber,
            String sectionTitle, float confidenceScore) {
        this.index = index;
        this.content = content;
        this.sourceUri = sourceUri;
        this.title = title;
        this.pageNumber = pageNumber;
        this.sectionTitle = sectionTitle;
        this.confidenceScore = confidenceScore;
    }

    public int getIndex() {
        return index;
    }

    public String getContent() {
        return content;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public String getTitle() {
        return title;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public float getConfidenceScore() {
        return confidenceScore;
    }
}
