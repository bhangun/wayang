package tech.kayys.wayang.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import tech.kayys.wayang.skill.DefaultSkill;
import tech.kayys.wayang.skill.Skill;
import tech.kayys.wayang.skill.SkillRegistry;
import tech.kayys.wayang.spi.service.DefinitionService;
import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.definition.AgentDefinition;
import tech.kayys.wayang.definition.SkillDefinition;
import tech.kayys.wayang.definition.SkillDescriptor;
import tech.kayys.wayang.definition.WorkflowDefinition;
import tech.kayys.wayang.extension.Capability;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.runtime.AgentRegistry;
import tech.kayys.wayang.runtime.GenericRegistry;

/**
 * Default Definition Service
 */
public class DefaultDefinitionService implements DefinitionService {
    
    private final AgentRegistry agentRegistry = new AgentRegistry();
    private final SkillRegistry skillRegistry = new SkillRegistry() {
        private final GenericRegistry<Skill> registry = new GenericRegistry<>();
        
        @Override
        public void register(Skill resource) {
            registry.register(resource);
        }
        
        @Override
        public void unregister(ResourceId id) {
            registry.unregister(id);
        }
        
        @Override
        public Optional<Skill> find(ResourceId id) {
            return registry.find(id);
        }
        
        @Override
        public Optional<Skill> findByName(String name) {
            return registry.findByName(name);
        }
        
        @Override
        public List<Skill> findAll() {
            return registry.findAll();
        }
        
        @Override
        public List<Skill> findByType(ResourceType type) {
            return registry.findByType(type);
        }
        
        @Override
        public List<Skill> findByLabel(String key, String value) {
            return registry.findByLabel(key, value);
        }
        
        @Override
        public boolean exists(ResourceId id) {
            return registry.exists(id);
        }
        
        @Override
        public boolean existsByName(String name) {
            return registry.existsByName(name);
        }
        
        @Override
        public int count() {
            return registry.count();
        }
        
        @Override
        public void clear() {
            registry.clear();
        }
        
        @Override
        public Optional<Skill> findByName(String name) {
            return registry.findByName(name);
        }
        
        @Override
        public List<Skill> findByCapability(Capability capability) {
            return registry.findAll().stream()
                .filter(skill -> skill.capabilities().contains(capability))
                .toList();
        }
        
        @Override
        public List<Skill> findByCategory(String category) {
            return registry.findAll().stream()
                .filter(skill -> skill.descriptor().categories().contains(category))
                .toList();
        }
    };
    private final GenericRegistry<WorkflowDefinition> workflowRegistry = new GenericRegistry<>();
    
    @Override
    public void registerAgent(AgentDefinition agent) {
        agentRegistry.register(agent);
    }
    
    @Override
    public void registerSkill(SkillDefinition skill) {
        // Registry handles Skill interface, not SkillDefinition
        // We need to wrap SkillDefinition as a Skill
        Skill skillWrapper = new DefaultSkill(new SkillDescriptor(
            skill.id().asString(),
            skill.metadata().name(),
            skill.metadata().version().toString(),
            skill.metadata().description(),
            Set.of(),
            Set.of(),
            Map.of(),
            Map.of(),
            List.of(),
            List.of()
        ));
        skillRegistry.register(skillWrapper);
    }
    
    @Override
    public void registerWorkflow(WorkflowDefinition workflow) {
        workflowRegistry.register(workflow);
    }
    
    @Override
    public Optional<AgentDefinition> findAgent(String id) {
        return agentRegistry.find(new ResourceId.AgentId(Id.fromString(id)));
    }
    
    @Override
    public Optional<AgentDefinition> findAgentByName(String name) {
        return agentRegistry.findByName(name);
    }
    
    @Override
    public Optional<SkillDefinition> findSkill(String id) {
        // We need to find the Skill and get its definition
        Optional<Skill> skill = skillRegistry.find(new ResourceId.SkillId(Id.fromString(id)));
        return skill.map(s -> {
            // Convert back to SkillDefinition
            return SkillDefinition.builder()
                .metadata(Metadata.builder()
                    .name(s.name())
                    .description(s.description())
                    .version(s.version())
                    .now()
                    .build())
                .build();
        });
    }
    
    @Override
    public Optional<SkillDefinition> findSkillByName(String name) {
        Optional<Skill> skill = skillRegistry.findByName(name);
        return skill.map(s -> SkillDefinition.builder()
            .metadata(Metadata.builder()
                .name(s.name())
                .description(s.description())
                .version(s.version())
                .now()
                .build())
            .build());
    }
    
    @Override
    public Optional<WorkflowDefinition> findWorkflow(String id) {
        return workflowRegistry.find(new ResourceId.WorkflowId(Id.fromString(id)));
    }
    
    @Override
    public Optional<WorkflowDefinition> findWorkflowByName(String name) {
        return workflowRegistry.findByName(name);
    }
    
    @Override
    public List<AgentDefinition> listAgents() {
        return agentRegistry.findAll();
    }
    
    @Override
    public List<SkillDefinition> listSkills() {
        // Convert Skills to SkillDefinitions
        return skillRegistry.findAll().stream()
            .map(skill -> SkillDefinition.builder()
                .metadata(Metadata.builder()
                    .name(skill.name())
                    .description(skill.description())
                    .version(skill.version())
                    .now()
                    .build())
                .build())
            .toList();
    }
    
    @Override
    public List<WorkflowDefinition> listWorkflows() {
        return workflowRegistry.findAll();
    }
    
    @Override
    public void removeAgent(String id) {
        agentRegistry.unregister(new ResourceId.AgentId(Id.fromString(id)));
    }
    
    @Override
    public void removeSkill(String id) {
        skillRegistry.unregister(new ResourceId.SkillId(Id.fromString(id)));
    }
    
    @Override
    public void removeWorkflow(String id) {
        workflowRegistry.unregister(new ResourceId.WorkflowId(Id.fromString(id)));
    }
}
