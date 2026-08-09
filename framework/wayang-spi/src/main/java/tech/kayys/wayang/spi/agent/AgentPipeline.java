package tech.kayys.wayang.spi.agent;

/**
 * AgentPipeline is the generic interface representing the execution pipeline of an Agent.
 * It is responsible for orchestrating the steps of reasoning, planning, tool execution, and output generation.
 */
public interface AgentPipeline {
    
    /**
     * Executes the pipeline with the given context.
     * 
     * @param context the execution context containing input, memory, and state.
     * @return the final result of the pipeline execution.
     */
    Object execute(Object context) throws Exception;

}
