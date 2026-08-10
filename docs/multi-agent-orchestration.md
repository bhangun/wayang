# Multi-Agent Orchestration (The Graph)

Wayang provides a state-machine execution graph (inspired by LangGraph) to orchestrate complex multi-agent workflows. This lives in the `wayang-orchestration` module.

## Core Concepts

### 1. `GraphState` and Reducers
Agents communicate by passing a shared `GraphState` object back and forth. Instead of blindly overwriting keys, `GraphState` allows you to register **Reducers** (e.g. `AppendReducer`, `OverwriteReducer`). 
For example, if multiple agents emit to a "messages" key, an `AppendReducer` will safely combine their outputs into a running chat history list.

### 2. Nodes and Edges
- **`GraphNode`**: A discrete unit of work that takes a `GraphState` and returns a `StateUpdate`.
- **`AgentNode`**: A specialized `GraphNode` that acts as a bridge, allowing you to drop standard Wayang Agents (like `ReActAgent`) directly into the workflow.
- **`ConditionalEdge`**: Defines dynamic routing logic. For example, a conditional edge can inspect the `GraphState` to decide whether to loop back to a "Researcher" node or proceed to the "Writer" node.

## Building a Workflow

You can construct a workflow using the fluent `WorkflowGraph` builder:

```java
CompiledWorkflow workflow = new WorkflowGraph()
    .addNode("researcher", new AgentNode(researchAgent, "query", "research_result"))
    .addNode("writer", new AgentNode(writerAgent, "research_result", "final_draft"))
    .addEdge("researcher", "writer")
    .addConditionalEdge("writer", state -> {
        if (state.get("needs_more_info")) return "researcher";
        return CompiledWorkflow.END;
    })
    .setEntryPoint("researcher")
    .compile();
```

## Stateful Checkpointing & Gamelan Integration

The `CompiledWorkflow` natively supports execution pausing and resuming via the `CheckpointStrategy` SPI. 
- **`InMemoryCheckpointStrategy`**: Used by default for fast, local CLI execution.
- **`JdbcCheckpointStrategy`**: Can be used to persist state mid-execution to a database.

**The Gamelan Synergy**: The Wayang Graph SDK acts as the lightweight programming model for AI workflows. When you are ready for heavy-duty, distributed cloud execution, this architecture is designed so that a compiled graph can be submitted to your **Gamelan Workflow Engine** clusters to execute the nodes reliably across Kafka and gRPC.
