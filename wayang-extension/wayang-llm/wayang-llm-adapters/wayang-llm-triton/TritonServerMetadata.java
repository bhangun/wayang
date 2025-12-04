package tech.kayys.wayang.models.adapter.triton.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TritonServerMetadata {
    private String name;
    private String version;
    private List<String> extensions;
}