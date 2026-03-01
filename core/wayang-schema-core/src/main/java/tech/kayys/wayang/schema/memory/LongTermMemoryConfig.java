package tech.kayys.wayang.schema.memory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LongTermMemoryConfig extends BaseMemoryConfig {
    // Inherits all common memory parameters without adding specialized ones
}
