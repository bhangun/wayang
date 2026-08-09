package tech.kayys.wayang.spi.sandbox;

import java.util.HashMap;
import java.util.Map;

public class SandboxConfiguration {
    private String workingDirectory;
    private String image; // Relevant for Docker
    private Map<String, String> environmentVariables = new HashMap<>();
    
    public String getWorkingDirectory() { return workingDirectory; }
    public void setWorkingDirectory(String workingDirectory) { this.workingDirectory = workingDirectory; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public Map<String, String> getEnvironmentVariables() { return environmentVariables; }
    public void setEnvironmentVariables(Map<String, String> env) { this.environmentVariables = env; }
    
    public void addEnvironmentVariable(String key, String value) {
        this.environmentVariables.put(key, value);
    }
}
