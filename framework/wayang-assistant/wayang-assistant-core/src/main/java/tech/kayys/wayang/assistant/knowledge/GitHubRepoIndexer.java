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
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads (as a ZIP archive) and indexes source code from public GitHub repositories.
 *
 * <p>The raw source files are stored under a configurable cache directory and are re-downloaded
 * whenever the cache TTL expires. Only text files (Java, Kotlin, YAML, JSON, XML, Markdown, .properties)
 * below a size threshold are indexed to avoid saturating the search index.
 *
 * <p>Configured repos (via {@code application.properties}):
 * <ul>
 *   <li>https://github.com/bhangun/wayang.git   → wayang source</li>
 *   <li>https://github.com/bhangun/gollek.git    → gollek source</li>
 *   <li>https://github.com/bhangun/gamelan.git   → gamelan source</li>
 * </ul>
 */
@ApplicationScoped
public class GitHubRepoIndexer {

    private static final Logger LOG = Logger.getLogger(GitHubRepoIndexer.class);

    /** Maximum size of a single file to index (in bytes – 512 KB). */
    private static final long MAX_FILE_BYTES = 512 * 1024L;

    private static final Set<String> INDEXED_EXTENSIONS = Set.of(
            ".java", ".kt", ".md", ".yaml", ".yml", ".json", ".xml", ".properties", ".toml");

    @ConfigProperty(name = "wayang.assistant.cache.dir", defaultValue = "${user.home}/.wayang/assistant-cache")
    String cacheBaseDir;

    @ConfigProperty(name = "wayang.assistant.cache.ttl-hours", defaultValue = "24")
    int cacheTtlHours;

    private final ConcurrentHashMap<String, Instant> lastFetch = new ConcurrentHashMap<>();

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Ensure the given repository is downloaded and cached locally.
     * Returns the root Path of the cached source files.
     */
    public Path ensureCached(KnowledgeSource source) {
        String resolvedBase = cacheBaseDir.replace("${user.home}", System.getProperty("user.home"));
        Path cacheDir = Paths.get(resolvedBase, source.id());

        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            LOG.errorf("Cannot create cache dir %s: %s", cacheDir, e.getMessage());
            return cacheDir;
        }

        Instant last = lastFetch.get(source.id());
        boolean stale = last == null || Duration.between(last, Instant.now()).toHours() >= cacheTtlHours;

        if (stale) {
            LOG.infof("Downloading GitHub repo '%s' from %s", source.name(), source.baseUrl());
            downloadAndExtract(source.baseUrl(), cacheDir);
            lastFetch.put(source.id(), Instant.now());
        } else {
            LOG.debugf("Using cached source for '%s' (cached %d min ago)",
                    source.name(), Duration.between(last, Instant.now()).toMinutes());
        }

        return cacheDir;
    }

    /**
     * Return all indexable file paths under the cached directory.
     * Only files with recognised extensions and under the size threshold are returned.
     */
    public List<Path> indexedFiles(Path cacheDir) {
        if (!Files.exists(cacheDir)) return List.of();
        try {
            return Files.walk(cacheDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return INDEXED_EXTENSIONS.stream().anyMatch(name::endsWith);
                    })
                    .filter(p -> {
                        try { return Files.size(p) <= MAX_FILE_BYTES; }
                        catch (IOException e) { return false; }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.errorf("Error listing cached files in %s: %s", cacheDir, e.getMessage());
            return List.of();
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private void downloadAndExtract(String repoUrl, Path targetDir) {
        // Convert git URL to GitHub archive URL: https://github.com/user/repo/archive/refs/heads/main.zip
        String archiveUrl = repoUrl
                .replace(".git", "")
                .replace("git@github.com:", "https://github.com/")
                + "/archive/refs/heads/main.zip";

        LOG.infof("Downloading archive: %s", archiveUrl);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(archiveUrl))
                    .timeout(Duration.ofMinutes(3))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                LOG.warnf("Archive download failed HTTP %d for %s", response.statusCode(), archiveUrl);
                return;
            }

            extractZip(response.body(), targetDir);
            LOG.infof("Extracted repo archive to %s", targetDir);

        } catch (Exception e) {
            LOG.errorf("Failed to download/extract %s: %s", archiveUrl, e.getMessage());
        }
    }

    private void extractZip(InputStream zipStream, Path targetDir) throws IOException {
        // Wipe existing content to avoid stale files
        if (Files.exists(targetDir)) {
            try (var walk = Files.walk(targetDir)) {
                walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
        }
        Files.createDirectories(targetDir);

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipStream))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) { zis.closeEntry(); continue; }

                String name = entry.getName();
                String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";

                if (!INDEXED_EXTENSIONS.contains(ext) || entry.getSize() > MAX_FILE_BYTES) {
                    zis.closeEntry();
                    continue;
                }

                // Strip the leading top-level folder (e.g. "wayang-main/")
                String relativePath = name.contains("/") ? name.substring(name.indexOf('/') + 1) : name;
                Path outPath = targetDir.resolve(relativePath);
                Files.createDirectories(outPath.getParent());

                try (OutputStream out = Files.newOutputStream(outPath)) {
                    zis.transferTo(out);
                }
                zis.closeEntry();
            }
        }
    }
}
