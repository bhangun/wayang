package tech.kayys.wayang.spi.skill;

import tech.kayys.wayang.spi.plugin.ExtensionPoint;

/**
 * Skill represents a modular rule, guideline, or instruction set for an Agent.
 * This is the base SPI for Wayang-Projects/Skills-Pool.
 */
public interface Skill extends ExtensionPoint {
    
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
