package tech.kayys.wayang.models.kserve.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KServe V2 Model Ready Response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelReadyResponse {
    
    /**
     * Model name
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * Whether model is ready
     */
    @JsonProperty("ready")
    private Boolean ready;
}