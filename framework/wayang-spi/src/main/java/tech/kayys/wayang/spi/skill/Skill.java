package tech.kayys.wayang.spi.skill;

import tech.kayys.wayang.extension.Extension;

/**
 * Skill represents a set of related tools and behaviors.
 * This is the base SPI for Wayang-Projects/Wayang-Skills.
 */
public interface Skill extends Extension {
    
    /**
     * Gets the unique identifier of the skill.
     */
    String getId();
    
    /**
     * Gets the context or instructions provided by this skill.
     * @return Markdown or text instructions.
     */
    String getInstructions();
    
    /**
     * Optional trigger conditions for this skill to be dynamically loaded.
     */
    default String getTriggerDescription() {
        return "";
    }
}
