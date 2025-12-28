package tech.kayys.wayang.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.kayys.wayang.schema.node.*;
import tech.kayys.wayang.common.spi.Node;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Node
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeDTO {

    private UUID id;

    @NotBlank(message = "Node ID is required")
    private String nodeId;

    @NotBlank(message = "Node name is required")
    private String name;

    private String description;

    @NotBlank(message = "Node type is required")
    private String nodeType;

    private String nodeDescriptorVersion;
    private Map<String, Object> config;
    private List<PortDescriptor> inputs;
    private List<Outputs> outputs;
    private Map<String, Object> properties;
    private PositionDTO position;
    private UIMetadataDTO uiMetadata;
    private NodeStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public enum NodeStatus {
        ACTIVE, INACTIVE, DELETED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PositionDTO {
        private Double x;
        private Double y;
        private Double width;
        private Double height;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UIMetadataDTO {
        private String icon;
        private String color;
        private Boolean collapsed;
        private String layer;
    }

    public static NodeDTO from(Node node) {
        if (node == null)
            return null;

        PositionDTO positionDTO = null;
        if (node.getPosition() != null) {
            positionDTO = PositionDTO.builder()
                    .x(node.getPosition().getX())
                    .y(node.getPosition().getY())
                    .width(node.getPosition().getWidth())
                    .height(node.getPosition().getHeight())
                    .build();
        }

        UIMetadataDTO uiMetadataDTO = null;
        if (node.getUiMetadata() != null) {
            uiMetadataDTO = UIMetadataDTO.builder()
                    .icon(node.getUiMetadata().getIcon())
                    .color(node.getUiMetadata().getColor())
                    .collapsed(node.getUiMetadata().getCollapsed())
                    .layer(node.getUiMetadata().getLayer())
                    .build();
        }

        return NodeDTO.builder()
                .id(node.getId())
                .nodeId(node.getNodeId())
                .name(node.getName())
                .description(node.getDescription())
                .nodeType(node.getNodeType())
                .nodeDescriptorVersion(node.getNodeDescriptorVersion())
                .config(node.getConfig())
                .inputs(node.getInputs())
                .outputs(node.getOutputs())
                .properties(node.getProperties())
                .position(positionDTO)
                .uiMetadata(uiMetadataDTO)
                .status(NodeStatus.ACTIVE)
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .version(node.getVersion())
                .build();
    }

    public static NodeDTO from(NodeDefinition node) {
        if (node == null)
            return null;

        PositionDTO positionDTO = null;
        if (node.getUi() != null && node.getUi().getPosition() != null) {
            positionDTO = PositionDTO.builder()
                    .x(node.getUi().getPosition().getX())
                    .y(node.getUi().getPosition().getY())
                    .build();
        }

        Map<String, Object> configMap = new java.util.HashMap<>();
        if (node.getProperties() != null) {
            for (Object p : node.getProperties()) {
                try {
                    String pName = (String) p.getClass().getMethod("name").invoke(p);
                    Object pValue = p.getClass().getMethod("defaultValue").invoke(p);
                    configMap.put(pName, pValue != null ? pValue : "");
                } catch (Exception e) {
                    // Ignore elements that don't match the pattern
                }
            }
        }

        return NodeDTO.builder()
                .nodeId(node.getId())
                .name(node.getDisplayName())
                .nodeType(node.getType()) // It's a String
                .config(configMap)
                .position(positionDTO)
                .status(NodeStatus.ACTIVE)
                .build();
    }
}