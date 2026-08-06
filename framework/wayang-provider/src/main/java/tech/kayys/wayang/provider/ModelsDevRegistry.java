package tech.kayys.wayang.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for dynamically fetching and caching models from models.dev.
 */
public class ModelsDevRegistry {
    private static final Logger LOG = Logger.getLogger(ModelsDevRegistry.class);
    private static final String MODELS_DEV_URL = "https://models.dev/models.json";
    private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000L; // 24 hours

    private final ObjectMapper mapper;
    private final Map<String, ModelInfo> modelCache = new ConcurrentHashMap<>();
    private final Path cacheFilePath;

    private static final ModelsDevRegistry INSTANCE = new ModelsDevRegistry();

    public static ModelsDevRegistry getInstance() {
        return INSTANCE;
    }

    private ModelsDevRegistry() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String home = System.getProperty("user.home");
        Path configDir = Paths.get(home, ".wayang", "config");
        this.cacheFilePath = configDir.resolve("models.json");
        
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            LOG.error("Failed to create wayang config directory", e);
        }
        
        initializeCache();
    }

    private void initializeCache() {
        boolean fetched = false;
        if (shouldFetchFromRemote()) {
            fetched = fetchFromRemote();
        }
        
        if (!fetched) {
            loadFromLocalCache();
        }
    }

    private boolean shouldFetchFromRemote() {
        if (!Files.exists(cacheFilePath)) {
            return true;
        }
        try {
            long lastModified = Files.getLastModifiedTime(cacheFilePath).toMillis();
            return (System.currentTimeMillis() - lastModified) > CACHE_TTL_MS;
        } catch (IOException e) {
            return true;
        }
    }

    private boolean fetchFromRemote() {
        try {
            LOG.info("Fetching models from " + MODELS_DEV_URL);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MODELS_DEV_URL))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();
                    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String json = response.body();
                parseAndCacheModels(json);
                Files.writeString(cacheFilePath, json);
                LOG.info("Successfully fetched and cached models.dev database");
                return true;
            } else {
                LOG.errorf("Failed to fetch models: HTTP %d", response.statusCode());
            }
        } catch (Exception e) {
            LOG.error("Exception fetching models.dev database", e);
        }
        return false;
    }

    private void loadFromLocalCache() {
        if (!Files.exists(cacheFilePath)) {
            LOG.warn("No local models cache found.");
            return;
        }
        try {
            String json = Files.readString(cacheFilePath);
            parseAndCacheModels(json);
            LOG.info("Loaded models database from local cache");
        } catch (IOException e) {
            LOG.error("Failed to read local models cache", e);
        }
    }

    private void parseAndCacheModels(String json) {
        try {
            Map<String, ModelInfo> parsed = mapper.readValue(json, new TypeReference<Map<String, ModelInfo>>() {});
            modelCache.clear();
            modelCache.putAll(parsed);
        } catch (Exception e) {
            LOG.error("Failed to parse models json", e);
        }
    }

    /**
     * Get a list of supported model names for a given provider (e.g., "openai", "google", "anthropic").
     */
    public List<String> getModelsForProvider(String providerPrefix) {
        String prefix = providerPrefix.toLowerCase() + "/";
        return modelCache.values().stream()
                .filter(m -> m.id != null && m.id.toLowerCase().startsWith(prefix))
                .map(m -> m.id.substring(prefix.length())) // Strip prefix
                .collect(Collectors.toList());
    }

    public static class ModelInfo {
        public String id;
        public String name;
        public String description;
        public String family;
        public Map<String, Object> limit;
    }
}
