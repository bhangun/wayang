package tech.kayys.wayang.skill;

import java.util.HashMap;
import java.util.Map;

import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.definition.SkillDescriptor;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Default Skill Implementation
 */
public class DefaultSkill implements Skill {
    
    private final SkillDescriptor descriptor;
    private final Map<String, Object> configuration = new HashMap<>();
    private boolean initialized = false;
    
    public DefaultSkill(SkillDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    @Override
    public String id() {
        return descriptor.id();
    }
    
    @Override
    public String name() {
        return descriptor.name();
    }
    
    @Override
    public String version() {
        return descriptor.version();
    }
    
    @Override
    public Metadata metadata() {
        return descriptor.metadata();
    }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Skill();
    }
    
    @Override
    public ResourceId resourceId() {
        return descriptor.resourceId();
    }
    
    @Override
    public SkillDescriptor descriptor() {
        return descriptor;
    }
    
    @Override
    public SkillResult execute(SkillContext context) throws Exception {
        // Default implementation - override in subclasses
        return SkillResult.success("Executed: " + descriptor.name());
    }
    
    @Override
    public void initialize() throws Exception {
        if (!initialized) {
            initialized = true;
        }
    }
    
    @Override
    public void shutdown() throws Exception {
        initialized = false;
    }
}
