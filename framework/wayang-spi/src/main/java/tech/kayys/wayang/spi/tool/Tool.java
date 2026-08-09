package tech.kayys.wayang.spi.tool;

import tech.kayys.wayang.spi.plugin.ExtensionPoint;

/**
 * Tool represents a generic capability that an Agent can execute.
 * This is the base SPI for Wayang-Projects/Tools-Pool.
 */
public interface Tool extends ExtensionPoint {
    
    /**
     * Gets the unique identifier of the tool.
     */
    String getId();
    
    /**
     * Gets the description of what the tool does, used by the reasoning engine.
     */
    String getDescription();
    
    /**
     * Executes the tool with the provided arguments.
     * 
     * @param arguments The arguments required by the tool.
     * @return The result of the execution.
     */
    Object execute(Object arguments) throws Exception;
}
