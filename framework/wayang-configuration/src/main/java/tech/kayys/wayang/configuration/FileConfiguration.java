package tech.kayys.wayang.configuration;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


/**
 * File-based Configuration
 */
public class FileConfiguration extends DefaultConfiguration {
    
    private final Path path;
    private final String format;
    private final ObjectMapper mapper;
    private final ScheduledExecutorService scheduler;
    private boolean watching = false;
    
    public FileConfiguration(Path path) throws Exception {
        this(path, detectFormat(path));
    }
    
    public FileConfiguration(Path path, String format) throws Exception {
        super(loadFile(path, format));
        this.path = path;
        this.format = format;
        this.mapper = createMapper(format);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    private static String detectFormat(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return "yaml";
        } else if (fileName.endsWith(".json")) {
            return "json";
        } else if (fileName.endsWith(".properties")) {
            return "properties";
        }
        return "yaml";
    }
    
    private static ObjectMapper createMapper(String format) {
        if ("yaml".equals(format) || "yml".equals(format)) {
            return new ObjectMapper(new YAMLFactory());
        } else if ("json".equals(format)) {
            return new ObjectMapper();
        } else {
            return new ObjectMapper(new YAMLFactory());
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadFile(Path path, String format) throws Exception {
        if (!Files.exists(path)) {
            return new LinkedHashMap<>();
        }
        
        ObjectMapper mapper = createMapper(format);
        String content = Files.readString(path);
        
        if ("properties".equals(format)) {
            Properties props = new Properties();
            props.load(new StringReader(content));
            Map<String, Object> result = new LinkedHashMap<>();
            for (String key : props.stringPropertyNames()) {
                result.put(key, props.getProperty(key));
            }
            return result;
        }
        
        return mapper.readValue(content, Map.class);
    }
    
    @Override
    public void reload() throws Exception {
        Map<String, Object> newValues = loadFile(path, format);
        values.clear();
        values.putAll(newValues);
        notifyListeners();
    }
    
    public void startWatching(long intervalMs) {
        if (watching) return;
        watching = true;
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (Files.exists(path)) {
                    long lastModified = Files.getLastModifiedTime(path).toMillis();
                    // Check if file has changed
                    reload();
                }
            } catch (Exception e) {
                // Log error
                System.err.println("Failed to reload configuration: " + e.getMessage());
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }
    
    public void stopWatching() {
        watching = false;
        scheduler.shutdown();
    }
    
    public void save() throws Exception {
        String content = mapper.writeValueAsString(values);
        Files.writeString(path, content);
    }
    
    public void save(Map<String, Object> newValues) throws Exception {
        values.clear();
        values.putAll(newValues);
        save();
        notifyListeners();
    }
    
    public Path path() {
        return path;
    }
    
    public String format() {
        return format;
    }
}