# Observability & Audit Logging

Wayang is designed for enterprise production environments, which means autonomous agents operate in a "glass box" rather than a black box.

## Agent Lifecycle Events (`wayang-agent`)

The `BaseReActAgent` and the `AgentBuilder` expose an `AgentListener` SPI. This allows you to hook into the agent's internal thought process and tool execution without tightly coupling the agent logic to a specific monitoring vendor.

Hooks include:
- `onThought(Agent, String)`
- `onToolStart(Agent, ToolInvocation)`
- `onToolResult(Agent, ToolInvocation, ToolResult)`
- `onAgentError(Agent, Throwable)`

## Audit Logging (`wayang-observability`)

To comply with enterprise security standards, Wayang provides an `AuditLogger` SPI.

The default implementation, `Slf4jAuditLogger`, outputs structured JSON logs whenever an agent executes a tool. **Crucially, it is configured by default to automatically mask sensitive arguments** (such as passwords, API tokens, and private keys) before they are written to disk or sent to a log aggregator like Splunk or Datadog.

## OpenTelemetry Integration (`wayang-observability-otel`)

For distributed tracing, include the `wayang-observability-otel` module. 

It provides the `OtelAgentListener`, an OpenTelemetry-native interceptor. When registered with an agent, it automatically maps the agent's internal lifecycle into standard OTel `Span`s. 

- The overall agent execution becomes the root span.
- Individual LLM generations and Tool executions become nested child spans.
- Tool arguments and results are attached as span attributes.

This makes Wayang instantly compatible with any OpenTelemetry collector, including Jaeger, Datadog APM, and LangSmith.
