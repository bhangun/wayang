package tech.kayys.wayang.skill.spi;

import tech.kayys.wayang.extension.Extension;

public interface Skill extends Extension {
    SkillDescriptor descriptor();
    SkillResult execute(SkillContext context);
}
