package tech.kayys.wayang.assistant.knowledge;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.DocSearchResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for searching Wayang documentation and knowledge bases.
 */
@ApplicationScoped
public class KnowledgeSearchService {

    private static final Logger LOG = Logger.getLogger(KnowledgeSearchService.class);

    @ConfigProperty(name = "wayang.docs.path", defaultValue = "website/wayang.github.io")
    String docsPath;

    @ConfigProperty(name = "wayang.assistant.max-doc-results", defaultValue = "10")
    int maxDocResults;

    @Inject
    public KnowledgeSourceRegistry knowledgeRegistry;

    public List<DocSearchResult> searchDocumentation(String query) {
        LOG.debugf("Searching all knowledge sources for: %s", query);

        String normalizedQuery = query.toLowerCase();
        List<String> keywords = Arrays.stream(normalizedQuery.split("\\s+"))
                .filter(k -> k.length() > 2)
                .distinct()
                .collect(Collectors.toList());

        if (knowledgeRegistry != null) {
            List<DocSearchResult> results = knowledgeRegistry.searchAll(query, keywords, normalizedQuery);
            if (!results.isEmpty()) return results;
        }

        return searchLocalDocs(normalizedQuery, keywords);
    }

    private List<DocSearchResult> searchLocalDocs(String normalizedQuery, List<String> keywords) {
        final List<DocSearchResult> results = new ArrayList<>();
        String effectiveDocsPath = (docsPath != null && !docsPath.isBlank())
                ? docsPath : "website/wayang.github.io";
        Path docsRoot = Paths.get(System.getProperty("user.dir"), effectiveDocsPath);

        if (!Files.exists(docsRoot)) {
            LOG.warnf("Local docs path not found: %s", docsRoot);
            return List.of(new DocSearchResult(
                    "Documentation not available", "",
                    "Check `wayang.docs.path` in application.properties."));
        }

        try {
            Files.walk(docsRoot)
                    .filter(p -> p.toString().endsWith(".md"))
                    .forEach(p -> {
                        try {
                            String content = Files.readString(p);
                            SearchResult match = findBestMatch(p, content, keywords, normalizedQuery);
                            if (match != null) {
                                results.add(new DocSearchResult(
                                        match.title, match.url, match.snippet, match.score, p.toString()));
                            }
                        } catch (IOException e) {
                            LOG.debugf("Error reading file %s: %s", p, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            LOG.errorf("Error walking docs directory: %s", e.getMessage());
        }

        results.sort(Comparator.comparingInt(DocSearchResult::getScore).reversed());
        List<DocSearchResult> limited = results.size() > maxDocResults
                ? new ArrayList<>(results.subList(0, maxDocResults)) : results;

        return limited.isEmpty()
                ? List.of(new DocSearchResult("No results found", "",
                        "No documentation found. Try different keywords."))
                : limited;
    }

    private SearchResult findBestMatch(Path path, String content, List<String> keywords, String fullQuery) {
        String[] lines = content.split("\\r?\\n");
        String title = extractTitle(content);
        int bestScore = 0;
        String bestSnippet = "";
        int bestLineIndex = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String normalized = line.toLowerCase();
            int score = 0;

            if (normalized.contains(fullQuery)) score += 10;

            for (String kw : keywords) {
                if (normalized.contains(kw)) score += 2;
            }

            if (line.trim().startsWith("#") && score > 0) score += 5;
            if ((line.trim().startsWith("```") || line.trim().startsWith("`")) && score > 0) score += 3;

            if (score > bestScore) {
                bestScore = score;
                bestSnippet = line.trim();
                bestLineIndex = i;
            }
        }

        if (bestScore > 0) {
            String context = provideContext(lines, bestLineIndex, 3);
            String url = path.getFileName().toString().replace(".md", "");
            return new SearchResult(title, url, context, bestScore);
        }

        return null;
    }

    private String extractTitle(String content) {
        for (String line : content.split("\\r?\\n")) {
            if (line.trim().startsWith("# ")) return line.replace("#", "").trim();
        }
        return "Untitled";
    }

    private String provideContext(String[] lines, int idx, int ctxLines) {
        int start = Math.max(0, idx - ctxLines);
        int end = Math.min(lines.length, idx + ctxLines + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) sb.append(lines[i].trim()).append(" ");
        String ctx = sb.toString();
        return ctx.length() > 400 ? ctx.substring(0, 400) + "…" : ctx;
    }

    static class SearchResult {
        final String title, url, snippet;
        final int score;

        SearchResult(String title, String url, String snippet, int score) {
            this.title = title;
            this.url = url;
            this.snippet = snippet;
            this.score = score;
        }
    }
}
