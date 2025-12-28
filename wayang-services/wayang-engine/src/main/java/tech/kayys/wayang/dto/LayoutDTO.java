package tech.kayys.wayang.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object for Workflow Layout
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LayoutDTO {
    private Map<String, Object> zoom;
    private Map<String, Object> offset;

    public static LayoutDTO from(Object layout) {
        // Placeholder for layout conversion
        return new LayoutDTO();
    }
}
