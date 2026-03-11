package tech.kayys.wayang.project.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Descriptor for a Wayang project, used by the assistant to generate project structures.
 */
public class ProjectDescriptor {
    
    private String name;
    private String description;
    private String groupId;
    private String artifactId;
    private String version;
    private String intent;
    private List<String> capabilities;
    private Map<String, Object> configuration;
    private List<WorkflowDescriptor> workflows;
    
    public ProjectDescriptor() {
        this.capabilities = new ArrayList<>();
        this.configuration = new HashMap<>();
        this.workflows = new ArrayList<>();
        this.groupId = "tech.kayys.wayang";
        this.version = "1.0.0-SNAPSHOT";
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getArtifactId() {
        return artifactId;
    }
    
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    public List<String> getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }
    
    public Map<String, Object> getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
    
    public List<WorkflowDescriptor> getWorkflows() {
        return workflows;
    }
    
    public void setWorkflows(List<WorkflowDescriptor> workflows) {
        this.workflows = workflows;
    }
    
    @Override
    public String toString() {
        return "ProjectDescriptor{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", capabilities=" + capabilities +
                ", workflows=" + workflows.size() +
                '}';
    }
}
