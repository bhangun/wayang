package tech.kayys.wayang.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.kayys.wayang.domain.Workflow;
import tech.kayys.wayang.schema.workflow.WorkflowDefinition;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Workflow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowDTO {

    private UUID id;

    @NotBlank(message = "Workflow name is required")
    @Size(max = 255, message = "Workflow name must not exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private UUID workspaceId;
    private String tenantId;
    private Workflow.WorkflowStatus status;
    private List<NodeDTO> nodes;
    private List<EdgeDTO> edges;
    private LayoutDTO layout;
    private List<String> tags;
    private Instant publishedAt;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private Long version;

    public static WorkflowDTO from(Workflow workflow, boolean includeNodes) {
        if (workflow == null)
            return null;

        WorkflowDTOBuilder builder = WorkflowDTO.builder()
                .id(workflow.id)
                .name(workflow.name)
                .description(workflow.description)
                .workspaceId(workflow.workspace != null ? workflow.workspace.id : null)
                .tenantId(workflow.tenantId)
                .status(workflow.status)
                .tags(workflow.tags)
                .publishedAt(workflow.publishedAt)
                .createdAt(workflow.createdAt)
                .createdBy(workflow.createdBy)
                .updatedAt(workflow.updatedAt)
                .version(workflow.entityVersion);

        if (includeNodes && workflow.definition != null) {
            if (workflow.definition.getNodes() != null) {
                builder.nodes(workflow.definition.getNodes().stream()
                        .map(NodeDTO::from)
                        .collect(Collectors.toList()));
            }
            if (workflow.definition.getEdges() != null) {
                builder.edges(workflow.definition.getEdges().stream()
                        .map(EdgeDTO::from)
                        .collect(Collectors.toList()));
            }
        }

        return builder.build();
    }
}