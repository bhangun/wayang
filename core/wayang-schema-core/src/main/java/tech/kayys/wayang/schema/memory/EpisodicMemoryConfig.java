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
public class EpisodicMemoryConfig extends BaseMemoryConfig {

    @JsonProperty(defaultValue = "general")
    @JsonPropertyDescription("The type of event being remembered (e.g., conversation, milestone, standard).")
    private String eventType;
}
