package tech.kayys.wayang.models.kserve.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * KServe V2 Server Metadata Response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerMetadataResponse {
    
    /**
     * Server name
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * Server version
     */
    @JsonProperty("version")
    private String version;
    
    /**
     * Supported extensions
     */
    @JsonProperty("extensions")
    private List<String> extensions;
}