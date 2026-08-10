# Human-in-the-Loop (HITL) & Guardrails

To safely deploy autonomous agents, Wayang provides robust interception mechanisms to prevent destructive actions and enforce corporate policies.

## 1. The `ApprovalStrategy` SPI

In `wayang-agent`, the `ApprovalStrategy` interface allows you to intercept a tool *before* it executes.

```java
public interface ApprovalStrategy {
    void requestApproval(Agent agent, ToolInvocation invocation) throws ApprovalRequiredException;
}
```

If an interceptor throws `ApprovalRequiredException`, the agent's execution loop is paused.

## 2. HITL Delegation (`wayang-hitl`)

The `HitlApprovalStrategy` automatically bridges the Agent execution loop with the `wayang-hitl` task management system.

1. Tools can be marked as dangerous via their `Capability.requiresApproval()` flag (e.g., a `bash` or `sql_execute` tool).
2. When the agent attempts to run a protected tool, `HitlApprovalStrategy` creates a `HumanTask` (status: `PENDING`) and suspends the agent.
3. A human manager can review the task via the UI/CLI.
4. **Resumption**: Resumption mechanics are configurable via `ResumeStrategy`. By default (`ManualResumeStrategy`), the client application must manually resume the agent. However, this can be swapped to an `EventBusResumeStrategy` to automatically re-hydrate and resume the agent when a `TaskApprovedEvent` is fired on the event bus.

## 3. Guardrails Engine (`wayang-guardrails`)

The `GuardrailApprovalStrategy` integrates the agent loop with the `GuardrailsEngine`.

1. Before a tool executes, its arguments are scanned by the engine (checking for PII, Toxicity, Prompt Injection, etc.).
2. If a policy violation is detected, a `GuardrailFallbackStrategy` is invoked.
3. **Configurable Fallbacks**:
   - `HitlEscalationFallbackStrategy` (Default): Escalates the violation to a `HumanTask`, allowing a human manager to manually override the guardrail.
   - `HardBlockFallbackStrategy`: Throws a `SecurityException` and immediately aborts the agent's action with zero tolerance.
