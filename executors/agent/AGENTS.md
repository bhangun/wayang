# Wayang Agent Executor — Skill-Based Architecture

## Overview

The Wayang Agent executor provides a **unified, skill-based framework** for executing
intelligent agents within the GAMELAN workflow engine. Instead of separate Java classes
for each agent type, agents are defined as **skill definitions** — data-driven personas
that include prompts, parameters, and tool configurations.

**Module Structure**:
- **`agent-core`** — Domain models, skill definitions, skill registry, built-in skill JSON templates
- **`agent-core-executor`** — Runtime execution: `SkillBasedAgentExecutor` + `OrchestratorSkillExecutor`

---

## Architecture

```
                         ┌─────────────────────────────┐
                         │     UI Canvas / API          │
                         │  (drag agent node, pick skill)│
                         └─────────┬───────────────────┘
                                   │ skillId + instruction
                                   ▼
┌──────────────┐   resolve   ┌─────────────────────────┐
│ SkillRegistry│◄────────────│ SkillBasedAgentExecutor  │
│              │             │ (executor type: "agent") │
│ ┌──────────┐ │             └──────┬──────────────────┘
│ │common    │ │                    │
│ │coder     │ │    ┌───────────────┼──────────────┐
│ │planner   │ │    ▼               ▼              ▼
│ │analytics │ │  render         inference       result
│ │evaluator │ │  prompts        (Gollek)
│ │orchestr. │ │    │
│ │custom... │ │    ▼
│ └──────────┘ │  SkillPromptRenderer
└──────────────┘  (uses wayang-prompt module)
```

### How It Works

1. User creates an agent node on the UI canvas and assigns a **skill** (built-in or custom)
2. Task context contains `skillId` + `instruction` + optional parameters
3. `SkillBasedAgentExecutor` looks up the `SkillDefinition` from `SkillRegistry`
4. `SkillPromptRenderer` renders system + user prompts from the skill definition
5. `GollekInferenceService` executes the inference with the skill's provider/temperature/model defaults
6. Result is returned to the workflow engine

---

## Skill Definition Format

Skills are defined as JSON files (classpath resources, database, or user-created in the UI):

```json
{
  "id": "security-keycloak-expert",
  "name": "Security & Keycloak Expert",
  "description": "Use this agent for security architecture, Keycloak IAM, OAuth2/OIDC...",
  "category": "custom",
  "systemPrompt": "You are a Senior Security Architect specializing in IAM...",
  "subSkillPrompts": {
    "AUDIT": "You are a security auditor. Review the system for vulnerabilities...",
    "CONFIGURE": "You are a Keycloak configuration expert..."
  },
  "userPromptTemplate": "Task: {{instruction}}\n\nContext:\n{{context}}",
  "temperature": 0.3,
  "maxTokens": 4096,
  "defaultProvider": "tech.kayys/anthropic-provider",
  "fallbackProvider": "tech.kayys/ollama-provider",
  "tools": ["api-caller", "code-reviewer"],
  "metadata": {
    "color": "Red",
    "icon": "shield",
    "version": "1.0.0"
  }
}
```

### Key Fields

| Field | Required | Description |
|-------|----------|-------------|
| `id` | ✅ | Unique skill identifier |
| `systemPrompt` | ✅ | The agent's persona / instructions |
| `subSkillPrompts` | ❌ | Task-type-specific prompts (selected via `taskType` in context) |
| `userPromptTemplate` | ❌ | Template with `{{placeholders}}` rendered via wayang-prompt engine |
| `temperature` | ❌ | Default inference temperature (0.0–1.0) |
| `maxTokens` | ❌ | Default max tokens |
| `defaultProvider` | ❌ | Default LLM provider ID |
| `tools` | ❌ | Tool IDs this agent can use |
| `orchestration` | ❌ | Orchestration config (makes this an orchestrator skill) |

---

## Built-in Skills

| Skill ID | Replaces | Sub-Skills | Default Provider |
|----------|----------|------------|------------------|
| `common` | CommonAgentExecutor | DATA_PROCESS, API_CALL, VALIDATE, GENERAL | ollama |
| `coder` | CoderAgentExecutor | GENERATE, REVIEW, REFACTOR, DEBUG, TEST, EXPLAIN, DOCUMENT, OPTIMIZE | openai |
| `planner` | PlannerAgentExecutor | HIERARCHICAL, CHAIN_OF_THOUGHT, TREE_OF_THOUGHT, REACT, PLAN_AND_EXECUTE, ADAPTIVE | anthropic |
| `analytics` | AnalyticAgentExecutor | DESCRIPTIVE, DIAGNOSTIC, PREDICTIVE, PRESCRIPTIVE, STATISTICAL, PATTERN, ANOMALY, TREND | openai |
| `evaluator` | EvaluatorAgentExecutor | QUALITY, CORRECTNESS, SAFETY, COMPARISON, RUBRIC | anthropic |
| `orchestrator` | OrchestratorAgentExecutor | DELEGATE, SYNTHESIZE, ROUTING, COORDINATE | anthropic |

---

## Usage

### Task Context (Workflow YAML)

```yaml
workflow:
  name: code-review-pipeline
  nodes:
    - id: review-code
      type: agent-task
      executor: agent              # ← unified executor type
      config:
        skillId: coder             # ← pick any skill
        taskType: REVIEW           # ← sub-skill selection
        instruction: "Review this Java class for security issues"
        code: |
          public class UserService { ... }
```

### Creating a Custom Skill (JSON)

```json
{
  "id": "devops-k8s-expert",
  "name": "DevOps & Kubernetes Expert",
  "description": "Kubernetes deployment, Helm charts, CI/CD pipelines",
  "category": "custom",
  "systemPrompt": "You are a Senior DevOps Engineer specializing in Kubernetes...",
  "temperature": 0.3,
  "maxTokens": 4096,
  "defaultProvider": "tech.kayys/anthropic-provider"
}
```

### Orchestrator

```yaml
workflow:
  name: full-project-analysis
  nodes:
    - id: orchestrate
      type: agent-task
      executor: agent-orchestrator
      config:
        skillId: orchestrator
        objective: "Analyze and improve the authentication module"
        orchestrationType: SEQUENTIAL
        agentTasks:
          - skillId: planner
            taskType: HIERARCHICAL
            instruction: "Create an improvement plan for the auth module"
          - skillId: coder
            taskType: REVIEW
            instruction: "Review the current auth implementation"
          - skillId: evaluator
            taskType: QUALITY
            instruction: "Evaluate the review findings"
```

---

## Backward Compatibility

The unified executor supports legacy agent type names via automatic mapping:

| Legacy `agentType` | Mapped `skillId` |
|---------------------|-----------------|
| `agent-coder` / `coder-agent` | `coder` |
| `agent-planner` / `planner-agent` | `planner` |
| `analytics-agent` / `analytic-agent` | `analytics` |
| `evaluator-agent` | `evaluator` |
| `common-agent` | `common` |
| `orchestrator-agent` | `orchestrator` |

Existing workflows that specify `agentType` instead of `skillId` will continue to work.

---

## Key Classes

| Class | Module | Purpose |
|-------|--------|---------|
| `SkillDefinition` | agent-core | Data model for skill definitions |
| `SkillRegistry` / `DefaultSkillRegistry` | agent-core | Runtime skill management |
| `SkillPromptRenderer` | agent-core | Prompt rendering (integrates with wayang-prompt) |
| `BuiltInSkillLoader` | agent-core | Loads built-in JSON skills on startup |
| `SkillBasedAgentExecutor` | agent-core-executor | Unified executor for all non-orchestrator agents |
| `OrchestratorSkillExecutor` | agent-core-executor | Multi-agent orchestration executor |
| `AbstractAgentExecutor` | agent-core-executor | Base class (guardrails, memory, lifecycle) |
| `MCPAgentExecutor` | agent-core-executor | MCP/A2A protocol support |

---

## Migration from Old Architecture

The old `agents/` directory (6 individual modules) is excluded from the Maven build but
not deleted. To migrate:

1. **Replace `agentType` with `skillId`** in workflow definitions
2. **Custom agent logic** → create a custom skill JSON instead of a new Java module
3. **Custom prompts** → define them in `subSkillPrompts` in the skill JSON
4. **Custom tools** → list tool IDs in the skill's `tools` array
