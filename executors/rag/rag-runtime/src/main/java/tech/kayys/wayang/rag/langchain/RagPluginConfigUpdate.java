package tech.kayys.gamelan.executor.rag.langchain;

public record RagPluginConfigUpdate(
        String selectionStrategy,
        String enabledIds,
        String order,
        String tenantEnabledOverrides,
        String tenantOrderOverrides,
        Boolean normalizeLowercase,
        Integer normalizeMaxQueryLength,
        Double lexicalRerankOriginalWeight,
        Double lexicalRerankLexicalWeight,
        Boolean lexicalRerankAnnotateMetadata,
        String safetyBlockedTerms,
        String safetyMask) {
}
