package tech.kayys.wayang.skill.spi;

import java.util.List;
import java.util.Optional;
import tech.kayys.wayang.registry.Registry;

public interface SkillRegistry extends Registry<Skill> {
    Optional<Skill> findById(String id);
    List<Skill> findByCategory(String category);
}
