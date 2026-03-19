package tech.kayys.wayang.assistant.knowledge;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches and caches documentation pages from remote GitHub Pages documentation sites.
 *
 * <p>On first access (or after the cache TTL expires) the service downloads the site's
 * markdown files by following links discovered in the site's index page. Content is stored
 * in a local cache directory and re-used on subsequent calls without hitting the network.
 *
 * <p>Supported sites (configured via {@code application.properties}):
 * <ul>
 *   <li>https://wayang-ai.github.io</li>
 *   <li>https://gollek-ai.github.io</li>
 *   <li>https://gamelan-ai.github.io</li>
 * </ul>
 */
@ApplicationScoped
public class RemoteDocsFetcher {

    private static final Logger LOG = Logger.getLogger(RemoteDocsFetcher.class);

    @ConfigProperty(name = "wayang.assistant.cache.dir", defaultValue = "${user.home}/.wayang/assistant-cache")
    String cacheBaseDir;

    @ConfigProperty(name = "wayang.assistant.cache.ttl-hours", defaultValue = "24")
    int cacheTtlHours;

    /** Tracks the last time each source was fetched. */
    private final ConcurrentHashMap<String, Instant> lastFetch = new ConcurrentHashMap<>();

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    // Known entry-point paths for each site (page slugs to crawl)
    private static final Map<String, List<String>> SITE_ENTRY_POINTS = Map.of(
            "wayang-ai.github.io", List.of("", "docs", "docs/index", "docs/quickstart"),
            "gollek-ai.github.io",  List.of("", "docs", "docs/index", "docs/quickstart"),
            "gamelan-ai.github.io", List.of("", "docs", "docs/index", "docs/quickstart")
    );

    /**
     * Ensure that docs for the given {@link KnowledgeSource} are locally cached.
     * Returns the path to the directory containing cached markdown files.
     */
    public Path ensureCached(KnowledgeSource source) {
        String resolvedCacheBase = cacheBaseDir.replace("${user.home}", System.getProperty("user.home"));
        Path cacheDir = Paths.get(resolvedCacheBase, source.id());

        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            LOG.errorf("Cannot create cache dir %s: %s", cacheDir, e.getMessage());
            return cacheDir;
        }

        Instant last = lastFetch.get(source.id());
        boolean stale = last == null || Duration.between(last, Instant.now()).toHours() >= cacheTtlHours;

        if (stale) {
            LOG.infof("Fetching remote docs for '%s' from %s", source.name(), source.baseUrl());
            fetchSite(source.baseUrl(), cacheDir, source.id());
            lastFetch.put(source.id(), Instant.now());
        } else {
            LOG.debugf("Using cached docs for '%s' (cached %d min ago)",
                    source.name(), Duration.between(last, Instant.now()).toMinutes());
        }

        return cacheDir;
    }

    // -----------------------------------------------------------------------
    // Internal fetching
    // -----------------------------------------------------------------------

    private void fetchSite(String baseUrl, Path cacheDir, String sourceId) {
        String host = extractHost(baseUrl);
        List<String> entryPoints = SITE_ENTRY_POINTS.getOrDefault(host, List.of("", "docs"));

        Set<String> visited = new HashSet<>();
        for (String entry : entryPoints) {
            String url = baseUrl.endsWith("/")
                    ? baseUrl + entry
                    : baseUrl + (entry.isEmpty() ? "" : "/" + entry);
            fetchPage(url, baseUrl, cacheDir, visited, 0);
        }
    }

    private void fetchPage(String url, String baseUrl, Path cacheDir, Set<String> visited, int depth) {
        if (depth > 3 || visited.contains(url)) return;
        visited.add(url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.debugf("Skipping %s – HTTP %d", url, response.statusCode());
                return;
            }

            String html = response.body();
            String markdown = htmlToMarkdown(html, url);

            if (!markdown.isBlank()) {
                String filename = urlToFilename(url, baseUrl) + ".md";
                Path outFile = cacheDir.resolve(filename);
                Files.writeString(outFile, markdown);
                LOG.debugf("Cached %s → %s", url, outFile.getFileName());
            }

            // Follow relative links (shallow crawl)
            if (depth < 2) {
                extractLinks(html, baseUrl).stream()
                        .filter(link -> !visited.contains(link))
                        .limit(30)
                        .forEach(link -> fetchPage(link, baseUrl, cacheDir, visited, depth + 1));
            }

        } catch (Exception e) {
            LOG.debugf("Could not fetch %s: %s", url, e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String htmlToMarkdown(String html, String sourceUrl) {
        // Strip scripts, styles, nav, footer — keep content
        String body = stripTags(html, "script", "style", "nav", "footer", "header");
        // Remove all remaining HTML tags
        String text = body.replaceAll("<[^>]+>", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#\\d+;", " ")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        if (text.length() < 100) return ""; // skip near-empty pages

        return "<!-- source: " + sourceUrl + " -->\n\n" + text;
    }

    private String stripTags(String html, String... tags) {
        String result = html;
        for (String tag : tags) {
            result = result.replaceAll("(?is)<" + tag + "[^>]*>.*?</" + tag + ">", " ");
        }
        return result;
    }

    private List<String> extractLinks(String html, String baseUrl) {
        List<String> links = new ArrayList<>();
        Matcher m = Pattern.compile("href=[\"']([^\"'#?]+)[\"']").matcher(html);
        while (m.find()) {
            String href = m.group(1).trim();
            if (href.startsWith("http")) {
                if (href.startsWith(baseUrl)) links.add(href);
            } else if (href.startsWith("/")) {
                String origin = extractOrigin(baseUrl);
                links.add(origin + href);
            }
        }
        return links;
    }

    private String urlToFilename(String url, String baseUrl) {
        String relative = url.replace(baseUrl, "").replaceAll("[^a-zA-Z0-9_/-]", "_");
        if (relative.isEmpty() || relative.equals("/")) return "index";
        return relative.replaceAll("^/+", "").replaceAll("/+$", "").replace("/", "__");
    }

    private String extractHost(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getHost();
        } catch (Exception e) {
            return url;
        }
    }

    private String extractOrigin(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() + "://" + uri.getHost();
        } catch (Exception e) {
            return url;
        }
    }
}
