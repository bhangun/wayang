package tech.kayys.wayang.configuration;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class FileConfigurationSource implements ConfigurationSource {
    private final Path path;
    private final String format;
    
    public FileConfigurationSource(Path path) {
        this.path = path;
        this.format = detectFormat(path);
    }
    
    public FileConfigurationSource(Path path, String format) {
        this.path = path;
        this.format = format;
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
    
    @Override
    public Map<String, Object> load() throws Exception {
        if (!Files.exists(path)) {
            return Map.of();
        }
        
        ObjectMapper mapper;
        if ("yaml".equals(format) || "yml".equals(format)) {
            mapper = new ObjectMapper(new YAMLFactory());
        } else if ("json".equals(format)) {
            mapper = new ObjectMapper();
        } else {
            // Properties
            Properties props = new Properties();
            try (Reader reader = Files.newBufferedReader(path)) {
                props.load(reader);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            for (String key : props.stringPropertyNames()) {
                result.put(key, props.getProperty(key));
            }
            return result;
        }
        
        return mapper.readValue(path.toFile(), Map.class);
    }
}
