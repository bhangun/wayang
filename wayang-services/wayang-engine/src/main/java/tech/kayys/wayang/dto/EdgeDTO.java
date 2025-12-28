package tech.kayys.wayang.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object for Workflow Edge
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdgeDTO {
    private String id;
    private String from;
    private String to;
    private String fromPort;
    private String toPort;
    private String condition;
    private Map<String, Object> metadata;

    public static EdgeDTO from(tech.kayys.wayang.schema.node.EdgeDefinition edge) {
        if (edge == null)
            return null;
        return EdgeDTO.builder()
                .id(edge.getId())
                .from(edge.getFrom())
                .to(edge.getTo())
                .fromPort(edge.getFromPort())
                .toPort(edge.getToPort())
                .condition(edge.getCondition())
                .metadata(edge.getMetadata())
                .build();
    }
}
