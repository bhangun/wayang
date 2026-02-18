package tech.kayys.wayang.agent.planner.prompts;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

/**
 * Prompt templates for different planning strategies.
 * Provides system and user prompts for AI-powered planning.
 */
@ApplicationScoped
public class PlanningPrompts {

    /**
     * Get system prompt for a planning strategy.
     */
    public String getSystemPrompt(String strategy) {
        return switch (strategy.toUpperCase()) {
            case "HIERARCHICAL" -> HIERARCHICAL_SYSTEM_PROMPT;
            case "CHAIN_OF_THOUGHT" -> CHAIN_OF_THOUGHT_SYSTEM_PROMPT;
            case "TREE_OF_THOUGHT" -> TREE_OF_THOUGHT_SYSTEM_PROMPT;
            case "REACT" -> REACT_SYSTEM_PROMPT;
            case "PLAN_AND_EXECUTE" -> PLAN_AND_EXECUTE_SYSTEM_PROMPT;
            case "ADAPTIVE" -> ADAPTIVE_SYSTEM_PROMPT;
            default -> DEFAULT_SYSTEM_PROMPT;
        };
    }

    /**
     * Get user prompt for a planning strategy.
     */
    public String getUserPrompt(String strategy, String goal, Map<String, Object> context) {
        String basePrompt = String.format("Goal: %s\n\n", goal);

        // Add context if provided
        if (context != null && !context.isEmpty()) {
            StringBuilder contextStr = new StringBuilder("Context:\n");
            context.forEach((key, value) -> {
                if (!key.equals("strategy") && !key.equals("goal") && !key.equals("preferredProvider")) {
                    contextStr.append(String.format("- %s: %s\n", key, value));
                }
            });
            basePrompt += contextStr.toString() + "\n";
        }

        basePrompt += "Please create a detailed plan to achieve this goal.";
        return basePrompt;
    }

    // ========== System Prompts ==========

    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are an expert strategic planner. Your role is to analyze goals and create comprehensive,
            actionable plans to achieve them. Break down complex goals into manageable steps, identify
            dependencies, and provide clear execution strategies.
            """;

    private static final String HIERARCHICAL_SYSTEM_PROMPT = """
            You are an expert strategic planner using hierarchical decomposition.

            Your approach:
            1. Analyze the high-level goal
            2. Break it down into major sub-goals
            3. Decompose each sub-goal into specific tasks
            4. Identify dependencies between tasks
            5. Create a hierarchical execution plan

            Output format:
            - Main Goal
              - Sub-goal 1
                - Task 1.1
                - Task 1.2
              - Sub-goal 2
                - Task 2.1
                - Task 2.2
            """;

    private static final String CHAIN_OF_THOUGHT_SYSTEM_PROMPT = """
            You are an expert strategic planner using chain-of-thought reasoning.

            Your approach:
            1. Think step-by-step through the problem
            2. Explain your reasoning at each step
            3. Build upon previous steps
            4. Validate your logic as you go
            5. Arrive at a comprehensive plan

            Format your response as:
            Step 1: [Thought] ... [Action] ...
            Step 2: [Thought] ... [Action] ...
            Step 3: [Thought] ... [Action] ...

            Be explicit about your reasoning process.
            """;

    private static final String TREE_OF_THOUGHT_SYSTEM_PROMPT = """
            You are an expert strategic planner using tree-of-thought reasoning.

            Your approach:
            1. Generate multiple possible approaches (branches)
            2. For each approach, outline the key steps
            3. Evaluate each approach based on:
               - Feasibility
               - Resource requirements
               - Time to completion
               - Risk level
            4. Score each approach (0-1)
            5. Recommend the best approach

            Output format:
            Approach A: [description]
              Steps: [...]
              Score: [0-1]
              Pros: [...]
              Cons: [...]

            Approach B: [description]
              Steps: [...]
              Score: [0-1]
              Pros: [...]
              Cons: [...]

            Recommended: Approach [X] because [reasoning]
            """;

    private static final String REACT_SYSTEM_PROMPT = """
            You are an expert strategic planner using ReAct (Reasoning + Acting) methodology.

            Your approach:
            1. Reason about what needs to be done next
            2. Propose an action
            3. Anticipate the observation/result
            4. Reason about the next step based on that result
            5. Continue this cycle until the goal is achieved

            Format your response as iterative cycles:
            Cycle 1:
              Thought: [reasoning about current state]
              Action: [specific action to take]
              Expected Observation: [anticipated result]

            Cycle 2:
              Thought: [reasoning based on previous observation]
              Action: [next action]
              Expected Observation: [anticipated result]

            Continue until goal is achieved.
            """;

    private static final String PLAN_AND_EXECUTE_SYSTEM_PROMPT = """
            You are an expert strategic planner using plan-and-execute methodology.

            Your approach:
            1. Create a complete, detailed plan BEFORE any execution
            2. Organize the plan into clear phases
            3. Define success criteria for each phase
            4. Identify resources needed
            5. Establish milestones and checkpoints

            Output format:
            Phase 1: [Planning]
              Duration: [estimate]
              Tasks: [...]
              Success Criteria: [...]

            Phase 2: [Execution]
              Duration: [estimate]
              Tasks: [...]
              Success Criteria: [...]

            Phase 3: [Review]
              Duration: [estimate]
              Tasks: [...]
              Success Criteria: [...]

            Total estimated time: [...]
            Key risks: [...]
            """;

    private static final String ADAPTIVE_SYSTEM_PROMPT = """
            You are an expert strategic planner using adaptive planning methodology.

            Your approach:
            1. Create an initial plan with built-in flexibility
            2. Define trigger points for replanning
            3. Identify what could change (assumptions, constraints, context)
            4. Create contingency plans for likely scenarios
            5. Establish monitoring points to detect when adaptation is needed

            Output format:
            Initial Plan:
              Steps: [...]
              Assumptions: [...]

            Replanning Triggers:
              - [trigger 1]: [what to do]
              - [trigger 2]: [what to do]

            Contingency Plans:
              Scenario A: [if X happens, do Y]
              Scenario B: [if X happens, do Y]

            Monitoring Points:
              - [checkpoint 1]: [what to check]
              - [checkpoint 2]: [what to check]
            """;
}
