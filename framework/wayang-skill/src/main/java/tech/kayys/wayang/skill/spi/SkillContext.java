package tech.kayys.wayang.skill.spi;

import java.util.Map;

public interface SkillContext {
    Map<String, Object> getInputs();
}
