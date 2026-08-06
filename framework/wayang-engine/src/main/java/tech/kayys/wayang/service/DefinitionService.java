package tech.kayys.wayang.service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;
import java.util.Optional;

import tech.kayys.wayang.definition.AgentDefinition;
import tech.kayys.wayang.definition.SkillDefinition;
import tech.kayys.wayang.definition.WorkflowDefinition;

/**
 * Definition Service - manages definitions
 */
public interface DefinitionService {
    void registerAgent(AgentDefinition agent);
    void registerSkill(SkillDefinition skill);
    void registerWorkflow(WorkflowDefinition workflow);
    
    Optional<AgentDefinition> findAgent(String id);
    Optional<AgentDefinition> findAgentByName(String name);
    Optional<SkillDefinition> findSkill(String id);
    Optional<SkillDefinition> findSkillByName(String name);
    Optional<WorkflowDefinition> findWorkflow(String id);
    Optional<WorkflowDefinition> findWorkflowByName(String name);
    
    List<AgentDefinition> listAgents();
    List<SkillDefinition> listSkills();
    List<WorkflowDefinition> listWorkflows();
    
    void removeAgent(String id);
    void removeSkill(String id);
    void removeWorkflow(String id);
}
