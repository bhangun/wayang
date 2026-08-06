package tech.kayys.wayang.skill.spi;

import java.util.Map;
import java.util.Set;
import tech.kayys.wayang.definition.Definition;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Reference;
import tech.kayys.wayang.identity.ResourceId;

public abstract class SkillDefinition extends Definition {
    public SkillDefinition(ResourceId id, Metadata metadata, Set<Reference> dependencies, Map<String, Object> configuration) {
        super(id, metadata, dependencies, configuration);
    }
    
    public abstract SkillDescriptor descriptor();
}
