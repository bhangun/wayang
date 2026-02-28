package tech.kayys.wayang.eip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EnricherDto(
        @JsonProperty("sources") List<EnrichmentSourceDto> sources,
        @JsonProperty("mergeStrategy") String mergeStrategy) {
}
