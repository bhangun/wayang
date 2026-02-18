package tech.kayys.gamelan.executor.rag.langchain;

public interface RagPluginTuningConfig {

    boolean normalizeQueryLowercase();

    int normalizeQueryMaxLength();

    double lexicalRerankOriginalWeight();

    double lexicalRerankLexicalWeight();

    boolean lexicalRerankAnnotateMetadata();

    String safetyFilterBlockedTerms();

    String safetyFilterMask();
}
