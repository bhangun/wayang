Searched for "hermes"
Searched for files: *hermes*
Searched for files: *
Searched for files: *
Searched for files: *.java
Searched for files: *.java
Viewed README.md:1-161

**Yes**, Wayang provides native, first-class support for the **Hermes-style agent architecture** (inspired by Nous Research Hermes Agent) across both the framework and runtime.

It is implemented as a dedicated agent mode and module suite in the platform:

---

### 1. Architectural Modules

| Module | Location | Purpose |
|---|---|---|
| **`agent-hermes`** | [`Wayang-Hermess/agent-hermes`](Wayang-Hermess/agent-hermes) | Core Hermes engine: decorating `AgentOrchestrator`, dynamic skill learning/refinement, sub-agent delegation, and memory reflection |
| **`wayang-hermes`** | [`Families/wayang/modules/wayang-hermes`](wayang/modules/wayang-hermes) | Runtime REST endpoints, diagnostics, journaling, and learning audit persistence |

---

### 2. Key Hermes Capabilities Supported in Wayang

1. **Dynamic Skill Learning & Continuous Self-Improvement**:
   - Analyzes execution trajectories post-run using [`HermesLearningPolicy`](Wayang-Hermess/agent-hermes/src/main/java/tech/kayys/wayang/agent/hermes/HermesLearningPolicy.java).
   - Distills repeatable procedures into standard, portable [`SKILL.md`](Wayang-Hermess/agent-hermes/src/main/java/tech/kayys/wayang/agent/hermes/HermesSkillMarkdownRenderer.java) artifacts.
   - Refines existing skills to avoid duplicate sprawl ([`HermesSkillReusePolicy`](Wayang-Hermess/agent-hermes/src/main/java/tech/kayys/wayang/agent/hermes/HermesSkillReusePolicy.java)).
2. **Persistent Epistemic Memory & Reflection**:
   - Formulates hierarchical memory context across sessions.
   - Triggers automated reflection cycles ([`HermesMemoryReflectionResolver`](Wayang-Hermess/agent-hermes/src/main/java/tech/kayys/wayang/agent/hermes/HermesMemoryReflectionResolver.java)) to consolidate insights.
3. **MCP (Model Context Protocol) & Toolset Posture**:
   - Integrates with Wayang's MCP bridge (`wayang-mcp`) to dynamically attach toolsets (`mcpServers`, stdio, and HTTP endpoints).
4. **Parallel Sub-Agent Delegation**:
   - [`HermesDelegationPlanner`](Wayang-Hermess/agent-hermes/src/main/java/tech/kayys/wayang/agent/hermes/HermesDelegationPlanner.java) turns complex, fan-out tasks into isolated sub-agent workstreams.
5. **Always-On Gateways & Scheduled Automation**:
   - [`HermesGatewayContextResolver`](Wayang-Hermess/agent-hermes/src/main/java/tech/kayys/wayang/agent/hermes/HermesGatewayContextResolver.java) normalizes multi-channel messaging (CLI, Slack, Telegram, Discord).
   - [`HermesAutomationIntentResolver`](Wayang-Hermess/agent-hermes/src/main/java/tech/kayys/wayang/agent/hermes/HermesAutomationIntentResolver.java) parses natural-language/cron schedules for background executions.

---

### 3. Enabling Hermes Mode in Runtime

Hermes mode is enabled via configuration properties (`application.properties` or environment variables):

```properties
wayang.agent.hermes.skill-learning-enabled=true
wayang.agent.hermes.skill-self-improvement-enabled=true
wayang.agent.hermes.persistent-memory-enabled=true
wayang.agent.hermes.mcp-enabled=true
wayang.agent.hermes.sub-agents-enabled=true
wayang.agent.hermes.preferred-provider=ollama
wayang.agent.hermes.default-toolsets=skills,memory,mcp,rag
wayang.agent.hermes.persistence-hints.definitions=database
wayang.agent.hermes.persistence-hints.artifacts=file-system
```