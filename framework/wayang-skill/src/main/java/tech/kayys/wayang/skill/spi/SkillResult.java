package tech.kayys.wayang.skill.spi;

import java.util.Map;

public interface SkillResult {
    Map<String, Object> getOutputs();
    boolean isSuccess();
}
