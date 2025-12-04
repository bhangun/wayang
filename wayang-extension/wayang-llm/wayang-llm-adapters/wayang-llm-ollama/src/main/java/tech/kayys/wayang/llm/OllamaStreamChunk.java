package tech.kayys.wayang.models.adapter.ollama.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaStreamChunk {
    private String model;
    private String response;
    private OllamaRequest.Message message;
    private Boolean done;
}