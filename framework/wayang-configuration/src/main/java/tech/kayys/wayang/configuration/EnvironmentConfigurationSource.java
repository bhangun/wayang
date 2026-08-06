package tech.kayys.wayang.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnvironmentConfigurationSource implements ConfigurationSource {
    private final String prefix;
    
    public EnvironmentConfigurationSource() {
        this("WAYANG_");
    }
    
    public EnvironmentConfigurationSource(String prefix) {
        this.prefix = prefix;
    }
    
    @Override
    public Map<String, Object> load() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                String path = key.substring(prefix.length())
                    .toLowerCase()
                    .replace('_', '.');
                result.put(path, entry.getValue());
            }
        }
        return result;
    }
}
