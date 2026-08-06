package tech.kayys.wayang.skill.spi;

import tech.kayys.wayang.descriptor.Descriptor;

public interface SkillDescriptor extends Descriptor {
    // Inherits id() as ResourceId
    // Inherits metadata() as Metadata
    // Inherits tags() as Set<String>
    
    String category();
}
