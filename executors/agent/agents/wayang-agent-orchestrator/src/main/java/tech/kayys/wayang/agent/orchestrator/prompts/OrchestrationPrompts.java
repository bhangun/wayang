package tech.kayys.wayang.agent.orchestrator.prompts;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

/**
 * Prompt templates for orchestration tasks.
 * Provides system and user prompts for multi-agent coordination.
 */
@ApplicationScoped
public class OrchestrationPrompts {

    /**
     * Get system prompt for orchestration task type.
     */
    public String getSystemPrompt(String taskType) {
        return switch (taskType.toUpperCase()) {
            case "DELEGATE" -> DELEGATE_TASK_PROMPT;
            case "SYNTHESIZE" -> SYNTHESIZE_RESULTS_PROMPT;
            case "ROUTING" -> ROUTING_DECISION_PROMPT;
            case "COORDINATE" -> COORDINATE_WORKFLOW_PROMPT;
            default -> DEFAULT_ORCHESTRATOR_PROMPT;
        };
    }

    /**
     * Get user prompt for orchestration task.
     */
    public String getUserPrompt(String taskType, String objective, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Objective: ").append(objective).append("\n\n");

        // Add available agents if provided
        Object agents = context.get("availableAgents");
        if (agents != null) {
            prompt.append("Available Agents:\n").append(agents).append("\n\n");
        }

        // Add context info
        if (context.containsKey("inputData")) {
            prompt.append("Input Data:\n").append(context.get("inputData")).append("\n\n");
        }

        if (context.containsKey("history")) {
            prompt.append("Execution History:\n").append(context.get("history")).append("\n\n");
        }

        return prompt.toString();
    }

    // ========== System Prompts ==========

    private static final String DEFAULT_ORCHESTRATOR_PROMPT = """
            You are an expert orchestrator agent. Your role is to coordinate other agents
            to achieve complex goals. You analyze objectives, break them down, delegate tasks,
            and synthesize results. Be strategic, efficient, and clear in your coordination.
            """;

    private static final String DELEGATE_TASK_PROMPT = """
            You are an expert at task delegation.

            Your goal:
            1. Analyze the complex objective
            2. Identify the best agent(s) for the job
            3. Decompose the objective into specific sub-tasks
            4. Define clear instructions for each agent

            Available Agents:
            - **Planner**: Strategic planning, task decomposition
            - **Coder**: Code generation, review, refactoring
            - **Analytics**: Data analysis, insights
            - **Common**: General tasks, data processing, validation

            Output format:
            ## Sub-tasks
            1. **[Agent Name]**
               - Task: [Specific instruction]
               - Context: [Necessary input data]
               - Dependencies: [Wait for Task X]

            2. **[Agent Name]**
               - ...

            ## Logic
            [Why this delegation strategy is optimal]
            """;

    private static final String SYNTHESIZE_RESULTS_PROMPT = """
            You are an expert at synthesizing results from multiple sources.

            Your goal:
            1. Review outputs from multiple agents
            2. Identify key findings and conflicts
            3. Merge information into a coherent final result
            4. Resolve any discrepancies
            5. Ensure the original objective is met

            Output format:
            ## Synthesized Result
            [Unified answer/output]

            ## Key Findings
            - [Finding 1]
            - [Finding 2]

            ## Resolution Notes
            [How conflicts were handled, if any]

            ## Completeness Check
            - [Requirement 1]: Met
            - [Requirement 2]: Met
            """;

    private static final String ROUTING_DECISION_PROMPT = """
            You are an expert at intelligent routing.

            Your goal:
            1. Analyze the incoming request
            2. Determine the most appropriate handling path
            3. Route to the right agent or workflow branch
            4. Justify the routing decision

            Output format:
            ## Decision
            **Route To**: [Agent/Path Name]

            ## Reasoning
            - Criteria A: [Match/No Match]
            - Criteria B: [Match/No Match]
            - Conclusion: [Why this path]

            ## Parameters
            - [Param 1]: [Value to pass]
            - [Param 2]: [Value to pass]
            """;

    private static final String COORDINATE_WORKFLOW_PROMPT = """
            You are an expert workflow coordinator.

            Your goal:
            1. Manage a multi-step workflow execution
            2. Monitor progress and state
            3. Handle dependencies and sequencing
            4. React to failures or blockers
            5. Adjust the plan if needed

            Output format:
            ## Current Status
            [Summary of where we are]

            ## Next Action
            **Step**: [Next step name]
            **Agent**: [Assigned Agent]
            **Instruction**: [What to do]

            ## Adjustments
            [Any changes to the original plan based on new context]
            """;
}
