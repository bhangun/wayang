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
public class ShortTermMemoryConfig extends BaseMemoryConfig {

    @JsonProperty(defaultValue = "10")
    @JsonPropertyDescription("The maximum history capacity to retain in short-term context.")
    private Integer maxCapacity;
}
