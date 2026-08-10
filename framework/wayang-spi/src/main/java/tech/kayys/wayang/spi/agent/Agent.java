package tech.kayys.wayang.spi.agent;

import tech.kayys.wayang.extension.Extension;

/**
 * Agent represents an autonomous entity configured with tools and skills.
 * This is the base SPI for Wayang-Projects/Wayang-Agents.
 */
public interface Agent extends Extension {
    
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

    /**
     * Resumes a paused execution (e.g. after a HITL approval).
     * 
     * @param threadId the unique identifier of the paused execution thread
     * @return the result of the resumed execution
     */
    default Object resume(String threadId) throws Exception {
        throw new UnsupportedOperationException("Resume is not supported by this agent implementation.");
    }
}
