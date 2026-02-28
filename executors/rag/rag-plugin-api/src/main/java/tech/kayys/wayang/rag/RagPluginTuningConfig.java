package tech.kayys.wayang.rag;

public interface RagPluginTuningConfig {

    boolean normalizeQueryLowercase();

    int normalizeQueryMaxLength();

    double lexicalRerankOriginalWeight();

    double lexicalRerankLexicalWeight();

    boolean lexicalRerankAnnotateMetadata();

    String safetyFilterBlockedTerms();

    String safetyFilterMask();
}
