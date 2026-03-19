package tech.kayys.wayang.assistant.knowledge;

import jakarta.annotation.PostConstruct;
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
 * Central registry for all assistant knowledge sources.
 *
 * <p>Manages three categories of sources:
 * <ol>
 *   <li><b>Local docs</b>  – the in-repo {@code website/wayang.github.io} folder</li>
 *   <li><b>Remote docs</b> – wayang-ai, gollek-ai, gamelan-ai GitHub Pages sites</li>
 *   <li><b>GitHub repos</b> – wayang, gollek, gamelan source code repositories</li>
 * </ol>
 *
 * <p>A unified {@link #searchAll(String, List, String)} API searches across all enabled
 * sources and returns merged, score-ranked results.
 */
@ApplicationScoped
public class KnowledgeSourceRegistry {

    private static final Logger LOG = Logger.getLogger(KnowledgeSourceRegistry.class);

    @ConfigProperty(name = "wayang.docs.path", defaultValue = "website/wayang.github.io")
    String localDocsPath;

    @ConfigProperty(name = "wayang.assistant.remote-docs.enabled", defaultValue = "true")
    boolean remoteDocsEnabled;

    @ConfigProperty(name = "wayang.assistant.github-rag.enabled", defaultValue = "true")
    boolean githubRagEnabled;

    @ConfigProperty(name = "wayang.assistant.max-doc-results", defaultValue = "10")
    int maxDocResults;

    @Inject
    RemoteDocsFetcher remoteDocsFetcher;

    @Inject
    GitHubRepoIndexer repoIndexer;

    private final List<KnowledgeSource> sources = new ArrayList<>();

    @PostConstruct
    void init() {
        // 1. Local docs (always enabled)
        sources.add(new KnowledgeSource(
                "wayang-local", "Wayang Local Docs",
                "https://wayang-ai.github.io",
                localDocsPath,
                KnowledgeSource.SourceType.LOCAL_DOCS));

        // 2. Remote documentation sites
        if (remoteDocsEnabled) {
            String cacheBase = System.getProperty("user.home") + "/.wayang/assistant-cache";
            sources.add(KnowledgeSource.remoteDocsSource(
                    "wayang-remote", "Wayang AI Docs",
                    "https://wayang-ai.github.io",
                    cacheBase + "/wayang-remote"));
            sources.add(KnowledgeSource.remoteDocsSource(
                    "gollek-remote", "Gollek AI Docs",
                    "https://gollek-ai.github.io",
                    cacheBase + "/gollek-remote"));
            sources.add(KnowledgeSource.remoteDocsSource(
                    "gamelan-remote", "Gamelan AI Docs",
                    "https://gamelan-ai.github.io",
                    cacheBase + "/gamelan-remote"));
        }

        // 3. GitHub source repos for RAG
        if (githubRagEnabled) {
            sources.add(KnowledgeSource.githubRepoSource(
                    "wayang-src", "Wayang Source Code",
                    "https://github.com/bhangun/wayang.git",
                    System.getProperty("user.home") + "/.wayang/assistant-cache/wayang-src"));
            sources.add(KnowledgeSource.githubRepoSource(
                    "gollek-src", "Gollek Source Code",
                    "https://github.com/bhangun/gollek.git",
                    System.getProperty("user.home") + "/.wayang/assistant-cache/gollek-src"));
            sources.add(KnowledgeSource.githubRepoSource(
                    "gamelan-src", "Gamelan Source Code",
                    "https://github.com/bhangun/gamelan.git",
                    System.getProperty("user.home") + "/.wayang/assistant-cache/gamelan-src"));
        }

        LOG.infof("Knowledge source registry initialised with %d sources.", sources.size());
    }

    /**
     * Returns all registered knowledge sources (read-only view).
     */
    public List<KnowledgeSource> getSources() {
        return Collections.unmodifiableList(sources);
    }

    /**
     * Search across all knowledge sources and return merged, de-duplicated results.
     *
     * @param query       the user query string
     * @param keywords    pre-tokenised keywords (may be empty – derived from query if so)
     * @param fullQuery   normalised lower-case version of the query
     * @return merged list sorted by score descending, limited to maxDocResults
     */
    public List<DocSearchResult> searchAll(String query, List<String> keywords, String fullQuery) {
        List<DocSearchResult> merged = new ArrayList<>();

        for (KnowledgeSource src : sources) {
            try {
                List<DocSearchResult> partial = switch (src.type()) {
                    case LOCAL_DOCS  -> searchLocalDocs(src, keywords, fullQuery);
                    case REMOTE_DOCS -> searchRemoteDocs(src, keywords, fullQuery);
                    case GITHUB_REPO -> searchGitHubRepo(src, keywords, fullQuery);
                };
                merged.addAll(partial);
            } catch (Exception e) {
                LOG.warnf("Error searching source '%s': %s", src.id(), e.getMessage());
            }
        }

        return merged.stream()
                .sorted(Comparator.comparingInt(DocSearchResult::getScore).reversed())
                .limit(maxDocResults)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Per-type search implementations
    // -----------------------------------------------------------------------

    private List<DocSearchResult> searchLocalDocs(KnowledgeSource src,
                                                   List<String> keywords, String fullQuery) {
        Path root = Paths.get(System.getProperty("user.dir"), src.localCachePath());
        return searchMarkdownDir(root, src, keywords, fullQuery);
    }

    private List<DocSearchResult> searchRemoteDocs(KnowledgeSource src,
                                                    List<String> keywords, String fullQuery) {
        Path cacheDir = remoteDocsFetcher.ensureCached(src);
        return searchMarkdownDir(cacheDir, src, keywords, fullQuery);
    }

    private List<DocSearchResult> searchGitHubRepo(KnowledgeSource src,
                                                    List<String> keywords, String fullQuery) {
        Path cacheDir = repoIndexer.ensureCached(src);
        List<Path> files = repoIndexer.indexedFiles(cacheDir);
        List<DocSearchResult> results = new ArrayList<>();

        for (Path file : files) {
            try {
                String content = Files.readString(file);
                DocSearchResult result = scoreFile(file, content, keywords, fullQuery, src);
                if (result != null) results.add(result);
            } catch (IOException e) {
                LOG.debugf("Cannot read cached file %s: %s", file, e.getMessage());
            }
        }

        return results;
    }

    private List<DocSearchResult> searchMarkdownDir(Path dir, KnowledgeSource src,
                                                     List<String> keywords, String fullQuery) {
        if (!Files.exists(dir)) return List.of();
        List<DocSearchResult> results = new ArrayList<>();
        try {
            Files.walk(dir)
                    .filter(p -> p.toString().endsWith(".md"))
                    .forEach(p -> {
                        try {
                            String content = Files.readString(p);
                            DocSearchResult r = scoreFile(p, content, keywords, fullQuery, src);
                            if (r != null) results.add(r);
                        } catch (IOException e) {
                            LOG.debugf("Cannot read %s: %s", p, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            LOG.errorf("Cannot walk %s: %s", dir, e.getMessage());
        }
        return results;
    }

    private DocSearchResult scoreFile(Path file, String content,
                                      List<String> keywords, String fullQuery,
                                      KnowledgeSource src) {
        String[] lines = content.split("\\r?\\n");
        int bestScore = 0;
        int bestLine = -1;

        for (int i = 0; i < lines.length; i++) {
            String normalised = lines[i].toLowerCase();
            int score = 0;
            if (normalised.contains(fullQuery)) score += 10;
            for (String kw : keywords) if (normalised.contains(kw)) score += 2;
            if (lines[i].trim().startsWith("#") && score > 0) score += 5;
            if (score > bestScore) { bestScore = score; bestLine = i; }
        }

        if (bestScore == 0) return null;

        String title = extractTitle(lines, src.name() + " / " + file.getFileName());
        String snippet = buildSnippet(lines, bestLine, 3);
        // URL: for remote sources, point to the public site; for repos, point to GitHub
        String url = buildUrl(src, file);

        return new DocSearchResult(title, url, snippet, bestScore, file.toString());
    }

    private String extractTitle(String[] lines, String fallback) {
        for (String line : lines) {
            if (line.trim().startsWith("# ")) return line.replace("#", "").trim();
        }
        return fallback;
    }

    private String buildSnippet(String[] lines, int idx, int ctx) {
        int start = Math.max(0, idx - ctx);
        int end = Math.min(lines.length, idx + ctx + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) sb.append(lines[i].trim()).append(" ");
        String s = sb.toString();
        return s.length() > 400 ? s.substring(0, 400) + "…" : s;
    }

    private String buildUrl(KnowledgeSource src, Path file) {
        return switch (src.type()) {
            case LOCAL_DOCS, REMOTE_DOCS -> src.baseUrl() + "/docs/" +
                    file.getFileName().toString().replace(".md", "");
            case GITHUB_REPO -> src.baseUrl()
                    .replace(".git", "")
                    .replace("https://github.com/", "https://github.com/")
                    + "/blob/main/" + file.getFileName();
        };
    }
}
