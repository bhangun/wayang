# Wayang Agent Executors - Architecture & Implementation Guide

## Overview

The Wayang Agent executor provides a comprehensive framework for executing intelligent agents within the GAMELAN workflow engine. It supports multiple agent types, orchestration strategies, and seamlessly integrates with the workflow execution pipeline.

**Module Structure**:
- **agent-core**: Domain models and types (records, enums, interfaces)
- **agent-executor**: Runtime execution implementing `gamelan-sdk-executor-core`

---

## Architecture

### Integration with GAMELAN Framework

The agent executors implement the [WorkflowExecutor](file://~/Workspace/workkayys/Products/Wayang/wayang-platform/workflow-gamelan/core/gamelan-sdk-executor-core/src/main/java/tech/kayys/gamelan/sdk/executor/core/WorkflowExecutor.java) interface from `gamelan-sdk-executor-core`:

```java
@Executor(
    executorType = "agent-type",
    communicationType = CommunicationType.GRPC,
    maxConcurrentTasks = 10
)
@ApplicationScoped
public class AgentExecutor extends AbstractAgentExecutor {
    // Implementation
}
```

**Key Components**:
1. `@Executor` annotation - Marks class as workflow executor
2. `WorkflowExecutor` interface - Defines execution contract
3. `ExecutorTransport` - Communication layer (gRPC/Kafka)
4. `NodeExecutionTask` - Input task from GAMELAN engine
5. `NodeExecutionResult` - Output result to GAMELAN engine

---

## Agent Executor Types

### 1. CommonAgentExecutor
**Purpose**: General-purpose task execution  
**Executor Type**: `common-agent`  
**Max Concurrent**: 10

**Capabilities**:
- ✅ Data processing
- ✅ API calling
- ✅ Data validation
- ✅ Generic task execution

**Specializations**:
- `data-processor` - Process and transform data
- `api-caller` - Make external API calls
- `validator` - Validate data against rules

**Example Task Context**:
```json
{
  "agentType": "common-agent",
  "specialization": "data-processor",
  "taskType": "transform",
  "parameters": {
    "input": {...},
    "transformation": "normalize"
  }
}
```

---

### 2. PlannerAgentExecutor
**Purpose**: Strategic planning and task decomposition  
**Executor Type**: `planner-agent`  
**Max Concurrent**: 5

**Planning Strategies**:

#### HIERARCHICAL
Top-down decomposition into sub-goals
- ✅ Goal analysis
- ✅ Sub-goal breakdown
- ✅ Execution plan creation

#### CHAIN_OF_THOUGHT
Step-by-step sequential reasoning
- ✅ Requirement understanding
- ✅ Dependency identification
- ✅ Step sequencing
- ✅ Plan validation

#### TREE_OF_THOUGHT
Multiple reasoning paths with evaluation
- ✅ Multiple approach exploration
- ✅ Path scoring
- ✅ Best path selection

#### REACT (Reasoning + Acting)
Iterative cycles of thought, action, observation
- ✅ Reason about next step
- ✅ Execute action
- ✅ Observe result
- ✅ Adapt plan

#### PLAN_AND_EXECUTE
Complete planning before execution
- ✅ Comprehensive planning phase
- ✅ Structured execution phase
- ✅ Review and verification

#### ADAPTIVE
Dynamic replanning based on context
- ✅ Initial plan generation
- ✅ Context monitoring
- ✅ Trigger-based replanning
- ✅ Plan adaptation

**Example Task Context**:
```json
{
  "agentType": "planner-agent",
  "strategy": "REACT",
  "goal": "Implement user authentication",
  "parameters": {
    "constraints": {...},
    "preferences": {...}
  }
}
```

---

### 3. CoderAgentExecutor
**Purpose**: Code generation, analysis, and refactoring  
**Executor Type**: `coder-agent`  
**Max Concurrent**: 8

**Code Capabilities**:

#### CODE_GENERATION
Generate code from specifications
- ✅ Multi-language support (Java, Python, JavaScript)
- ✅ Framework integration
- ✅ Code stub generation

#### CODE_REVIEW
Analyze and review code quality
- ✅ Style checking
- ✅ Performance analysis
- ✅ Best practice validation
- ✅ Issue detection

#### CODE_REFACTORING
Improve code structure and quality
- ✅ Method extraction
- ✅ Complexity reduction
- ✅ Naming improvements

#### BUG_FIXING
Identify and fix bugs
- ✅ Root cause analysis
- ✅ Code correction
- ✅ Test generation

#### TEST_GENERATION
Generate unit and integration tests
- ✅ Framework selection (JUnit, pytest)
- ✅ Coverage optimization
- ✅ Edge case handling

#### DOCUMENTATION
Generate code documentation
- ✅ Javadoc/JSDoc generation
- ✅ Example inclusion
- ✅ API documentation

#### CODE_EXPLANATION
Explain code functionality and complexity
- ✅ Algorithm explanation
- ✅ Complexity analysis
- ✅ Principle identification

#### PERFORMANCE_OPTIMIZATION
Optimize code performance
- ✅ Caching strategies
- ✅ Parallel processing
- ✅ Lazy loading
- ✅ Performance metrics

**Supported Languages**: Java, Python, JavaScript, TypeScript, Go

**Example Task Context**:
```json
{
  "agentType": "coder-agent",
  "capability": "CODE_GENERATION",
  "language": "Java",
  "codeContext": {
    "specification": "Create a REST API endpoint",
    "framework": "Spring Boot"
  }
}
```

---

### 4. AnalyticsAgentExecutor
**Purpose**: Data analysis and insights generation  
**Executor Type**: `analytics-agent`  
**Max Concurrent**: 6

**Analytics Capabilities**:

#### DESCRIPTIVE
*What happened?*
- ✅ Statistical summaries (mean, median, mode, stddev)
- ✅ Data distribution analysis
- ✅ Visualization recommendations

#### DIAGNOSTIC
*Why did it happen?*
- ✅ Root cause analysis
- ✅ Correlation detection
- ✅ Factor impact assessment

#### PREDICTIVE
*What will happen?*
- ✅ Time series forecasting (ARIMA)
- ✅ Trend prediction
- ✅ Confidence intervals
- ✅ Multiple period forecasts

#### PRESCRIPTIVE
*What should we do?*
- ✅ Action recommendations
- ✅ Priority ranking
- ✅ Impact estimation
- ✅ ROI calculation

#### STATISTICAL_ANALYSIS
Hypothesis testing and validation
- ✅ T-tests
- ✅ Chi-square tests
- ✅ ANOVA
- ✅ Significance testing

#### PATTERN_RECOGNITION
Identify recurring patterns
- ✅ Cyclical patterns
- ✅ Seasonal variation
- ✅ Frequency analysis
- ✅ Pattern strength scoring

#### ANOMALY_DETECTION
Detect outliers and anomalies
- ✅ Isolation Forest
- ✅ Z-score detection
- ✅ Severity classification
- ✅ Expected range calculation

#### TREND_ANALYSIS
Analyze trends and trajectories
- ✅ Trend direction
- ✅ Trend strength
- ✅ Change point detection
- ✅ Acceleration analysis

**Supported Data Formats**: JSON, CSV, Parquet

**Example Task Context**:
```json
{
  "agentType": "analytics-agent",
  "capability": "PREDICTIVE",
  "dataFormat": "JSON",
  "dataContext": {
    "timeSeries": [...],
    "horizon": 90
  }
}
```

---

### 5. OrchestratorAgentExecutor
**Purpose**: Multi-agent coordination and orchestration  
**Executor Type**: `orchestrator-agent`  
**Max Concurrent**: 3 (resource-intensive)

**Orchestration Types**:

#### SEQUENTIAL
Execute agents one after another
- ✅ Ordered execution
- ✅ Output chaining
- ✅ Failure propagation

#### PARALLEL
Execute all agents concurrently
- ✅ Maximum throughput
- ✅ Independent execution
- ✅ Fail-fast behavior

#### HIERARCHICAL
Tree-like delegation structure
- ✅ Multi-level coordination
- ✅ Top-down control
- ✅ Sub-task distribution

#### COLLABORATIVE
Agents work together on shared goal
- ✅ Synchronized execution
- ✅ Result merging
- ✅ Collective output

#### COMPETITIVE
Best result wins
- ✅ Multiple approaches
- ✅ Quality-based selection
- ✅ Performance comparison

#### DEBATE
Agents debate solutions
- ✅ Multi-round discussion
- ✅ Consensus building
- ✅ Confidence scoring

**Coordination Strategies**:
- `CENTRALIZED` - Single coordinator
- `DISTRIBUTED` - Peer-to-peer
- `HIERARCHICAL` - Multi-level management

**Example Task Context**:
```json
{
  "agentType": "orchestrator-agent",
  "orchestrationType": "PARALLEL",
  "coordinationStrategy": "CENTRALIZED",
  "agentTasks": [
    {
      "agentType": "planner-agent",
      "context": {...}
    },
    {
      "agentType": "coder-agent",
      "context": {...}
    }
  ]
}
```

---

## Configuration

### Dependencies

```xml
<dependency>
    <groupId>tech.kayys.wayang</groupId>
    <artifactId>agent-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>tech.kayys.gamelan</groupId>
    <artifactId>gamelan-sdk-executor-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>tech.kayys.gamelan</groupId>
    <artifactId>gamelan-engine-spi</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Executor Registration

Executors are automatically discovered via CDI `@ApplicationScoped` and `@Executor` annotations. The GAMELAN engine registers them during startup.

---

## Usage Patterns

### Direct Execution

```java
@Inject
CommonAgentExecutor commonAgent;

// Create task
NodeExecutionTask task = new NodeExecutionTask() {
    // Implement task properties
};

// Execute
Uni<NodeExecutionResult> result = commonAgent.execute(task);
```

### Workflow Integration

Agents are typically invoked from GAMELAN workflows:

```yaml
workflow:
  name: data-analysis-pipeline
  nodes:
    - id: analyze
      type: agent-task
      executor: analytics-agent
      config:
        agentType: analytics-agent
        capability: PREDICTIVE
        dataFormat: JSON
```

### Multi-Agent Orchestration

```java
// Orchestrator coordinates multiple agents
Map<String, Object> context = Map.of(
    "agentType", "orchestrator-agent",
    "orchestrationType", "COLLABORATIVE",
    "agentTasks", List.of(
        Map.of("agentType", "planner-agent", "strategy", "REACT"),
        Map.of("agentType", "coder-agent", "capability", "CODE_GENERATION")
    )
);
```

---

## Performance Characteristics

| Executor | Max Concurrent | Avg Latency | Resource Usage |
|----------|---------------|-------------|----------------|
| CommonAgent | 10 | Low (50ms) | Low |
| PlannerAgent | 5 | Medium (200ms) | Medium |
| CoderAgent | 8 | Medium (150ms) | Medium |
| AnalyticsAgent | 6 | High (500ms) | High |
| OrchestratorAgent | 3 | High (1s+) | Very High |

---

## Best Practices

### 1. Agent Selection
- Use **CommonAgent** for simple, generic tasks
- Use **PlannerAgent** for complex strategy problems
- Use **CoderAgent** for all code-related operations
- Use **AnalyticsAgent** for data-driven insights
- Use **OrchestratorAgent** for multi-step workflows

### 2. Orchestration Strategy
- **Sequential**: When order matters and dependencies exist
- **Parallel**: For independent tasks requiring speed
- **Hierarchical**: For complex multi-level coordination
- **Collaborative**: When agents need to share context
- **Competitive**: When multiple solutions need comparison
- **Debate**: For consensus-based decision making

### 3. Error Handling
All executors implement proper error handling:
- Exceptions are captured and returned as `NodeExecutionResult` with error
- `onError()` lifecycle hook for custom error handling
- Retry logic can be configured at GAMELAN engine level

### 4. Resource Management
- Respect `maxConcurrentTasks` limits
- Use appropriate orchestration type for workload
- Monitor executor health and performance

---

## Deployment

### Standalone Deployment
```bash
# Build executors
mvn clean package -f agent-executor/pom.xml

# Run with Quarkus
java -jar target/quarkus-app/quarkus-run.jar
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agent-executor
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: agent-executor
        image: wayang/agent-executor:latest
        env:
        - name: EXECUTOR_TYPE
          value: "all"
```

---

## Testing

Run tests:
```bash
mvn test -f agent-executor/pom.xml
```

Integration tests cover:
- ✅ All agent types
- ✅ All capabilities/strategies
- ✅ Orchestration patterns
- ✅ Error scenarios
- ✅ Concurrent execution

---

## Related Modules

- [`agent-core`](file://~/Workspace/workkayys/Products/Wayang/wayang-platform/wayang/executors/agent/agent-core) - Domain models
- [`gamelan-sdk-executor-core`](file://~/Workspace/workkayys/Products/Wayang/wayang-platform/workflow-gamelan/core/gamelan-sdk-executor-core) - Executor SDK
- [`gamelan-engine-spi`](file://~/Workspace/workkayys/Products/Wayang/wayang-platform/workflow-gamelan/core/gamelan-engine-spi) - Engine API

---

## Summary

The Wayang Agent executor framework provides:
- ✅ 5 specialized agent types with 30+ capabilities
- ✅ Seamless GAMELAN workflow integration
- ✅ 6 orchestration patterns for multi-agent coordination
- ✅ Production-ready with proper error handling and resource management
- ✅ Extensible architecture for custom agent types

Ready for use in complex workflow scenarios requiring intelligent task execution! 🚀
