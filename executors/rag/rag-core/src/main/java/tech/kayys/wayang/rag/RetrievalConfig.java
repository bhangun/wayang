
public record RetrievalConfig(
        String query, int topK, int finalK, double minScore, String strategy,
        boolean enableReranking, boolean enableDiversity, boolean enableQueryExpansion,
        Map<String, Object> filters, String storeType, String tenantId) {
}