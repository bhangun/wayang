package tech.kayys.wayang.schema.memory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SemanticMemoryConfig extends BaseMemoryConfig {

    @JsonProperty(defaultValue = "0.7")
    @JsonPropertyDescription("The minimum confidence score required for semantic facts.")
    private Double minConfidence;
}
