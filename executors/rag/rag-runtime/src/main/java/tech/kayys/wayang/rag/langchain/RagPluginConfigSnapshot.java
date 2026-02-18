package tech.kayys.gamelan.executor.rag.langchain;

public record RagPluginConfigSnapshot(
        String selectionStrategy,
        String enabledIds,
        String order,
        String tenantEnabledOverrides,
        String tenantOrderOverrides,
        boolean normalizeLowercase,
        int normalizeMaxQueryLength,
        double lexicalRerankOriginalWeight,
        double lexicalRerankLexicalWeight,
        boolean lexicalRerankAnnotateMetadata,
        String safetyBlockedTerms,
        String safetyMask) {
}
