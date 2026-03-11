package tech.kayys.wayang.assistant.agent;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import tech.kayys.wayang.project.api.ProjectDescriptor;
import tech.kayys.wayang.project.api.ProjectFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper service used by the internal Wayang assistant.
 * Provides documentation search, project generation, and error troubleshooting capabilities.
 */
@ApplicationScoped
public class WayangAssistantService {

    private static final Logger LOG = Logger.getLogger(WayangAssistantService.class);

    @ConfigProperty(name = "wayang.docs.path", defaultValue = "website/wayang.github.io")
    String docsPath;

    @ConfigProperty(name = "wayang.assistant.max-doc-results", defaultValue = "10")
    int maxDocResults;

    /**
     * Perform a search over the Wayang documentation site.
     */
    public List<DocSearchResult> searchDocumentation(String query) {
        LOG.debugf("Searching documentation for: %s", query);
        
        final List<DocSearchResult> results = new ArrayList<>();
        Path docsRoot = Paths.get(System.getProperty("user.dir"), docsPath);
        
        if (!Files.exists(docsRoot)) {
            LOG.warnf("Documentation path not found: %s", docsRoot);
            return Collections.singletonList(
                new DocSearchResult("Documentation not available", "", "System documentation path not configured")
            );
        }

        String normalizedQuery = query.toLowerCase();
        List<String> keywords = Arrays.stream(normalizedQuery.split("\\s+"))
                .filter(k -> k.length() > 2)
                .collect(Collectors.toList());

        try {
            Files.walk(docsRoot)
                    .filter(p -> p.toString().endsWith(".md"))
                    .forEach(p -> {
                        try {
                            String content = Files.readString(p);
                            SearchResult match = findBestMatch(p, content, keywords, normalizedQuery);
                            if (match != null) {
                                results.add(new DocSearchResult(
                                    match.title,
                                    match.url,
                                    match.snippet,
                                    match.score,
                                    p.toString()
                                ));
                            }
                        } catch (IOException e) {
                            LOG.debugf("Error reading file %s: %s", p, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            LOG.errorf("Error walking documentation directory: %s", e.getMessage());
        }

        // Sort by score and limit results
        results.sort(Comparator.comparingInt(DocSearchResult::getScore).reversed());

        List<DocSearchResult> limitedResults = results;
        if (results.size() > maxDocResults) {
            limitedResults = new ArrayList<>(results.subList(0, maxDocResults));
        }

        if (limitedResults.isEmpty()) {
            limitedResults.add(new DocSearchResult(
                "No results found",
                "",
                "No documentation pages found matching '" + query + "'. Try using different keywords."
            ));
        }
        
        return limitedResults;
    }

    private SearchResult findBestMatch(Path path, String content, List<String> keywords, String fullQuery) {
        String[] lines = content.split("\\r?\\n");
        String title = extractTitle(content);
        int bestScore = 0;
        String bestSnippet = "";
        int bestLineIndex = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String normalizedLine = line.toLowerCase();
            int score = 0;

            // Score for full query match
            if (normalizedLine.contains(fullQuery)) {
                score += 10;
            }

            // Score for keyword matches
            for (String keyword : keywords) {
                if (normalizedLine.contains(keyword)) {
                    score += 2;
                }
            }

            // Boost score for headers and important sections
            if (line.trim().startsWith("#") && score > 0) {
                score += 5;
            }

            if (score > bestScore) {
                bestScore = score;
                bestSnippet = line.trim();
                bestLineIndex = i;
            }
        }

        if (bestScore > 0) {
            String context = provideContext(lines, bestLineIndex, 2);
            String url = path.getFileName().toString().replace(".md", "");
            return new SearchResult(title, url, context, bestScore);
        }

        return null;
    }

    private String extractTitle(String content) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().startsWith("# ")) {
                return line.replace("#", "").trim();
            }
        }
        return "Untitled";
    }

    private String provideContext(String[] lines, int lineIndex, int contextLines) {
        StringBuilder context = new StringBuilder();
        int start = Math.max(0, lineIndex - contextLines);
        int end = Math.min(lines.length, lineIndex + contextLines + 1);
        
        for (int i = start; i < end; i++) {
            context.append(lines[i].trim()).append(" ");
        }
        
        return context.length() > 300 ? context.substring(0, 300) + "..." : context.toString();
    }

    /**
     * Generate a new Wayang project based on high-level user intent.
     */
    public ProjectDescriptor generateProject(String intent) {
        LOG.infof("Generating project from intent: %s", intent);
        
        ProjectDescriptor descriptor = ProjectFactory.fromIntent(intent);
        
        if (descriptor.getName() == null) {
            String name = generateNameFromIntent(intent);
            descriptor.setName(name);
            descriptor.setArtifactId(name.toLowerCase().replaceAll("\\s+", "-"));
        }
        
        if (descriptor.getDescription() == null) {
            descriptor.setDescription("Auto-generated Wayang project: " + intent);
        }
        
        if (descriptor.getCapabilities().contains("rag") && 
            descriptor.getCapabilities().contains("web-search")) {
            descriptor = ProjectFactory.createRagProject(descriptor.getName(), descriptor.getDescription());
            addWebSearchNode(descriptor);
        } else if (descriptor.getCapabilities().contains("rag")) {
            descriptor = ProjectFactory.createRagProject(descriptor.getName(), descriptor.getDescription());
        } else if (descriptor.getCapabilities().contains("web-search")) {
            descriptor = ProjectFactory.createWebSearchProject(descriptor.getName(), descriptor.getDescription());
        } else {
            descriptor = ProjectFactory.createAgentProject(descriptor.getName(), descriptor.getDescription());
        }
        
        LOG.infof("Generated project: %s with capabilities: %s", 
                  descriptor.getName(), descriptor.getCapabilities());
        
        return descriptor;
    }

    private String generateNameFromIntent(String intent) {
        String[] words = intent.split("\\s+");
        if (words.length >= 2) {
            return Arrays.stream(words)
                    .limit(3)
                    .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
        }
        return "Wayang " + intent.substring(0, Math.min(20, intent.length()));
    }

    private void addWebSearchNode(ProjectDescriptor descriptor) {
        if (!descriptor.getWorkflows().isEmpty()) {
            var workflow = descriptor.getWorkflows().get(0);
            
            var searchNode = new tech.kayys.wayang.project.api.NodeDescriptor();
            searchNode.setId("web-search-1");
            searchNode.setType("web-search-task");
            searchNode.setExecutor("web-search");
            searchNode.setDescription("Web search for real-time information");
            searchNode.getConfig().put("provider", "duckduckgo");
            searchNode.getConfig().put("maxResults", 5);
            
            workflow.getNodes().add(0, searchNode);
            
            var conn = new tech.kayys.wayang.project.api.ConnectionDescriptor("web-search-1", "agent-1");
            workflow.getConnections().add(conn);
        }
    }

    /**
     * Look up explanations or fixes for a given error message.
     */
    public ErrorTroubleshootingResult troubleshootError(String errorMessage) {
        LOG.infof("Troubleshooting error: %s", errorMessage);
        
        List<DocSearchResult> docResults = searchDocumentation(errorMessage);
        
        List<String> errorPatterns = extractErrorPatterns(errorMessage);
        for (String pattern : errorPatterns) {
            docResults.addAll(searchDocumentation(pattern));
        }
        
        String advice = generateTroubleshootingAdvice(errorMessage, docResults);
        
        return new ErrorTroubleshootingResult(errorMessage, advice, docResults);
    }

    private List<String> extractErrorPatterns(String errorMessage) {
        List<String> patterns = new ArrayList<>();
        
        if (errorMessage.contains(":")) {
            String beforeColon = errorMessage.split(":")[0].trim();
            if (beforeColon.contains(".")) {
                patterns.add(beforeColon.substring(beforeColon.lastIndexOf(".") + 1));
            }
        }
        
        if (errorMessage.matches(".*[A-Z]+-\\d+.*")) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[A-Z]+-\\d+");
            java.util.regex.Matcher matcher = pattern.matcher(errorMessage);
            while (matcher.find()) {
                patterns.add(matcher.group());
            }
        }
        
        return patterns;
    }

    private String generateTroubleshootingAdvice(String errorMessage, List<DocSearchResult> docResults) {
        StringBuilder advice = new StringBuilder();
        
        advice.append("## Troubleshooting Steps:\n\n");
        
        if (!docResults.isEmpty() && !"No results found".equals(docResults.get(0).getTitle())) {
            advice.append("### Relevant Documentation:\n");
            for (int i = 0; i < Math.min(3, docResults.size()); i++) {
                DocSearchResult result = docResults.get(i);
                advice.append(String.format("%d. **%s**: %s\n", 
                                          i + 1, result.getTitle(), result.getSnippet()));
            }
            advice.append("\n");
        }
        
        advice.append("### General Steps:\n");
        advice.append("1. Check your Wayang configuration files for syntax errors\n");
        advice.append("2. Verify that all required dependencies are included in your pom.xml\n");
        advice.append("3. Ensure the Quarkus application is running with correct profiles\n");
        advice.append("4. Review the logs for additional context around the error\n");
        advice.append("5. Check the Wayang documentation at https://wayang.github.io\n");
        
        String lowerError = errorMessage.toLowerCase();
        if (lowerError.contains("nullpointer") || lowerError.contains("null")) {
            advice.append("\n### Null-Related Error:\n");
            advice.append("- Check that all required configuration properties are set\n");
            advice.append("- Verify that injected beans are properly annotated\n");
        } else if (lowerError.contains("connection") || lowerError.contains("timeout")) {
            advice.append("\n### Connection-Related Error:\n");
            advice.append("- Verify that dependent services (PostgreSQL, Kafka) are running\n");
            advice.append("- Check network connectivity and firewall rules\n");
            advice.append("- Review connection timeout settings in application.properties\n");
        } else if (lowerError.contains("classnotfound") || lowerError.contains("noclassdeffound")) {
            advice.append("\n### Class Loading Error:\n");
            advice.append("- Ensure all required dependencies are in your classpath\n");
            advice.append("- Check for version conflicts in your Maven dependencies\n");
            advice.append("- Try running 'mvn clean install' to rebuild the project\n");
        }
        
        return advice.toString();
    }

    // Inner classes for structured results

    public static class DocSearchResult {
        private final String title;
        private final String url;
        private final String snippet;
        private final int score;
        private final String filePath;

        public DocSearchResult(String title, String url, String snippet) {
            this(title, url, snippet, 0, null);
        }

        public DocSearchResult(String title, String url, String snippet, int score, String filePath) {
            this.title = title;
            this.url = url;
            this.snippet = snippet;
            this.score = score;
            this.filePath = filePath;
        }

        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getSnippet() { return snippet; }
        public int getScore() { return score; }
        public String getFilePath() { return filePath; }
    }

    static class SearchResult {
        final String title;
        final String url;
        final String snippet;
        final int score;

        SearchResult(String title, String url, String snippet, int score) {
            this.title = title;
            this.url = url;
            this.snippet = snippet;
            this.score = score;
        }
    }

    public static class ErrorTroubleshootingResult {
        private final String errorMessage;
        private final String advice;
        private final List<DocSearchResult> documentationResults;

        public ErrorTroubleshootingResult(String errorMessage, String advice, List<DocSearchResult> documentationResults) {
            this.errorMessage = errorMessage;
            this.advice = advice;
            this.documentationResults = documentationResults;
        }

        public String getErrorMessage() { return errorMessage; }
        public String getAdvice() { return advice; }
        public List<DocSearchResult> getDocumentationResults() { return documentationResults; }
    }
}
