package tech.kayys.wayang.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.domain.Workflow;
import tech.kayys.wayang.model.LogicDefinition;
import tech.kayys.wayang.schema.LogicDefinitionDTO;
import tech.kayys.wayang.schema.NodeDTO;
import tech.kayys.wayang.schema.RuntimeConfigDTO;
import tech.kayys.wayang.schema.UIDefinitionDTO;
import tech.kayys.wayang.schema.UpdateWorkflowInput;
import tech.kayys.wayang.schema.WorkflowDTO;
import tech.kayys.wayang.schema.governance.RuntimeConfig;
import tech.kayys.wayang.schema.node.NodeDefinition;
import tech.kayys.wayang.schema.workflow.UIDefinition;

@ApplicationScoped
public class WorkflowMapper {

    public WorkflowDTO toDTO(Workflow workflow) {
        if (workflow == null)
            return null;
        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(workflow.id != null ? workflow.id.toString() : null);
        // Map other fields
        return dto;
    }

    public LogicDefinition toLogicEntity(LogicDefinitionDTO dto) {
        return dto == null ? null : new LogicDefinition();
    }

    public UIDefinition toUIEntity(UIDefinitionDTO dto) {
        return dto == null ? null : new UIDefinition();
    }

    public RuntimeConfig toRuntimeEntity(RuntimeConfigDTO dto) {
        return dto == null ? null : new RuntimeConfig();
    }

    public NodeDTO toNodeDTO(NodeDefinition node) {
        if (node == null)
            return null;
        NodeDTO dto = new NodeDTO();
        dto.setId(node.getId());
        dto.setName(node.getDisplayName());
        return dto;
    }

    public java.util.Map<String, Object> buildChangeSet(Workflow workflow,
            UpdateWorkflowInput input) {
        return java.util.Collections.emptyMap();
    }
}
