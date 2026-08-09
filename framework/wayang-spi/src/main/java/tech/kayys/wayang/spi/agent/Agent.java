package tech.kayys.wayang.spi.agent;

import tech.kayys.wayang.spi.plugin.ExtensionPoint;

/**
 * Agent represents an autonomous entity configured with tools and skills.
 * This is the base SPI for Wayang-Projects/Wayang-Agents.
 */
public interface Agent extends ExtensionPoint {
    
    /**
     * Gets the unique identifier of this agent.
     */
    String getId();
    
    /**
     * Returns the execution pipeline of this agent.
     */
    AgentPipeline getPipeline();
    
    /**
     * Initialize the agent with its configuration.
     */
    void initialize() throws Exception;
    
    /**
     * Process an input request asynchronously.
     */
    Object process(Object request) throws Exception;
}
