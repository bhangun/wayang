package tech.kayys.wayang.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

public class SystemPropertiesConfigurationSource implements ConfigurationSource {
    @Override
    public Map<String, Object> load() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith("wayang.")) {
                String path = key.substring(7); // Remove "wayang."
                result.put(path, entry.getValue());
            }
        }
        return result;
    }
}

