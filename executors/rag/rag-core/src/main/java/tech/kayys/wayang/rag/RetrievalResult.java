public record RetrievalResult(
        int resultsRetrieved, int finalResults,
        double avgScore, double maxScore, double minScore,
        List<String> contexts, List<Map<String, Object>> metadata,
        boolean reranked) {
}