package tech.kayys.wayang.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kayys.wayang.context.ContextData;
import tech.kayys.wayang.context.ContextProvider;
import tech.kayys.wayang.context.MemoryRecord;
import tech.kayys.wayang.evaluator.Evaluation;
import tech.kayys.wayang.evaluator.EvaluationStatus;
import tech.kayys.wayang.evaluator.Evaluator;
import tech.kayys.wayang.guard.GuardrailProvider;
import tech.kayys.wayang.guard.GuardrailResult;
import tech.kayys.wayang.inference.CompletionRequest;
import tech.kayys.wayang.inference.CompletionResult;
import tech.kayys.wayang.inference.InferenceProvider;
import tech.kayys.wayang.inference.Message;
import tech.kayys.wayang.input.InputProvider;
import tech.kayys.wayang.input.InputResult;
import tech.kayys.wayang.memory.MemoryProvider;
import tech.kayys.wayang.output.OutputProvider;
import tech.kayys.wayang.output.OutputResult;
import tech.kayys.wayang.output.OutputType;
import tech.kayys.wayang.planner.Plan;
import tech.kayys.wayang.planner.PlanStep;
import tech.kayys.wayang.planner.Planner;
import tech.kayys.wayang.reasoner.Reasoner;
import tech.kayys.wayang.reasoner.ReasoningResult;
import tech.kayys.wayang.resource.Artifact;
import tech.kayys.wayang.resource.JsonArtifact;
import tech.kayys.wayang.resource.TextArtifact;
import tech.kayys.wayang.spi.service.EventService;
import tech.kayys.wayang.tool.ToolExecutor;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;
import tech.kayys.wayang.workflow.WorkflowEngine;
import tech.kayys.wayang.event.Event;
import tech.kayys.wayang.event.EventPayload;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.execution.ExecutionError;
import tech.kayys.wayang.execution.ExecutionState;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Agent Pipeline - orchestrates the entire agent lifecycle.
 */
public class AgentPipeline {
    
    private static final Logger log = LoggerFactory.getLogger(AgentPipeline.class);
    
    private final InputProvider inputProvider;
    private final ContextProvider contextProvider;
    private final Planner planner;
    private final Reasoner reasoner;
    private final InferenceProvider inferenceProvider;
    private final ToolExecutor toolExecutor;
    private final MemoryProvider memoryProvider;
    private final WorkflowEngine workflowEngine;
    private final Evaluator evaluator;
    private final GuardrailProvider guardrailProvider;
    private final OutputProvider outputProvider;
    private final EventService eventService;
    private final ExecutorService executorService;
    
    private AgentPipeline(Builder builder) {
        this.inputProvider = builder.inputProvider;
        this.contextProvider = builder.contextProvider;
        this.planner = builder.planner;
        this.reasoner = builder.reasoner;
        this.inferenceProvider = builder.inferenceProvider;
        this.toolExecutor = builder.toolExecutor;
        this.memoryProvider = builder.memoryProvider;
        this.workflowEngine = builder.workflowEngine;
        this.evaluator = builder.evaluator;
        this.guardrailProvider = builder.guardrailProvider;
        this.outputProvider = builder.outputProvider;
        this.eventService = builder.eventService;
        this.executorService = builder.executorService != null 
            ? builder.executorService 
            : Executors.newCachedThreadPool();
    }
    
    /**
     * Execute the agent pipeline synchronously.
     */
    public ExecutionContext execute(ExecutionContext context) throws Exception {
        log.info("Starting agent pipeline execution: {}", context.id().asString());
        
        try {
            // Phase 1: Guardrails
            context = guardPhase(context);
            
            // Phase 2: Input
            context = inputPhase(context);
            
            // Phase 3: Context
            context = contextPhase(context);
            
            // Phase 4: Planning
            context = planningPhase(context);
            
            // Phase 5: Reasoning
            context = reasoningPhase(context);
            
            // Phase 6: Inference (LLM)
            context = inferencePhase(context);
            
            // Phase 7: Tools (if needed)
            context = toolPhase(context);
            
            // Phase 8: Memory
            context = memoryPhase(context);
            
            // Phase 9: Evaluation
            context = evaluationPhase(context);
            
            // Phase 10: Output
            context = outputPhase(context);
            
            // Phase 11: Complete
            context = context.withState(ExecutionState.COMPLETED);
            
            // Publish completion event
            publishCompletionEvent(context);
            
            log.info("Agent pipeline execution completed: {}", context.id().asString());
            return context;
            
        } catch (Exception e) {
            log.error("Agent pipeline execution failed: {}", context.id().asString(), e);
            context = context
                .withState(ExecutionState.FAILED)
                .withError(ExecutionError.of("PIPELINE_ERROR", e.getMessage(), "pipeline", e));
            publishFailureEvent(context, e);
            throw e;
        }
    }
    
    /**
     * Execute the agent pipeline asynchronously.
     */
    public CompletableFuture<ExecutionContext> executeAsync(ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    private ExecutionContext guardPhase(ExecutionContext context) throws Exception {
        if (guardrailProvider != null) {
            log.debug("Guard phase: {}", context.id().asString());
            GuardrailResult result = guardrailProvider.validate(context);
            publishEvent(context, new DomainEvent.PolicyChecked(
                "guardrail",
                "execution",
                "validate",
                context.principal().username(),
                result.passed()
            ));
            if (!result.passed()) {
                throw new SecurityException("Guardrail failed: " + result.message());
            }
        }
        return context;
    }
    
    private ExecutionContext inputPhase(ExecutionContext context) throws Exception {
        if (inputProvider != null) {
            log.debug("Input phase: {}", context.id().asString());
            InputResult inputResult = inputProvider.receive(context);
            context = context
                .withVariable("input", inputResult)
                .withArtifact(TextArtifact.of(inputResult.content(), "input"));
            publishEvent(context, new DomainEvent.InputReceived(
                inputResult.id(),
                inputResult.type().name(),
                inputResult.content()
            ));
        }
        return context;
    }
    
    private ExecutionContext contextPhase(ExecutionContext context) throws Exception {
        if (contextProvider != null) {
            log.debug("Context phase: {}", context.id().asString());
            ContextData contextData = contextProvider.load(context);
            context = context
                .withVariable("contextData", contextData)
                .withAttribute("contextDocuments", contextData.documents())
                .withAttribute("contextMemories", contextData.memories());
            publishEvent(context, new DomainEvent.ContextLoaded(
                contextData.id(),
                contextData.documents().size(),
                contextData.memories().size()
            ));
        }
        return context;
    }
    
    private ExecutionContext planningPhase(ExecutionContext context) throws Exception {
        if (planner != null) {
            log.debug("Planning phase: {}", context.id().asString());
            ContextData contextData = context.getVariable("contextData", ContextData.class);
            Plan plan = contextData != null 
                ? planner.createPlan(context, contextData)
                : planner.createPlan(context);
            context = context
                .withVariable("plan", plan)
                .withArtifact(JsonArtifact.of(plan.toString(), plan, "plan"));
            publishEvent(context, new DomainEvent.PlanCreated(
                plan.id(),
                plan.name(),
                plan.steps().size()
            ));
        }
        return context;
    }
    
    private ExecutionContext reasoningPhase(ExecutionContext context) throws Exception {
        if (reasoner != null) {
            log.debug("Reasoning phase: {}", context.id().asString());
            Plan plan = context.getVariable("plan", Plan.class);
            ContextData contextData = context.getVariable("contextData", ContextData.class);
            ReasoningResult result = contextData != null
                ? reasoner.reason(plan, context, contextData)
                : reasoner.reason(plan, context);
            context = context
                .withVariable("reasoningResult", result)
                .withArtifact(TextArtifact.of(result.conclusion(), "reasoning"));
            publishEvent(context, new DomainEvent.ReasoningCompleted(
                result.id(),
                result.conclusion(),
                result.confidence()
            ));
        }
        return context;
    }
    
    private ExecutionContext inferencePhase(ExecutionContext context) throws Exception {
        if (inferenceProvider != null) {
            log.debug("Inference phase: {}", context.id().asString());
            CompletionRequest request = buildCompletionRequest(context);
            CompletionResult result = inferenceProvider.generate(request);
            context = context
                .withVariable("completionResult", result)
                .withArtifact(TextArtifact.of(result.getContent(), "completion"));
            if (result.usage() != null) {
                context = context.withAttribute("tokensUsed", result.usage().totalTokens());
            }
            publishEvent(context, new DomainEvent.CompletionFinished(
                result.model(),
                context.id().asString(),
                result.getContent(),
                result.usage() != null ? result.usage().totalTokens() : 0,
                result.usage() != null ? 0.0 : 0.0
            ));
        }
        return context;
    }
    
    private ExecutionContext toolPhase(ExecutionContext context) throws Exception {
        if (toolExecutor != null) {
            log.debug("Tool phase: {}", context.id().asString());
            // Check if tools are needed from the plan
            Plan plan = context.getVariable("plan", Plan.class);
            if (plan != null) {
                for (PlanStep step : plan.steps()) {
                    if ("tool".equals(step.type())) {
                        ToolInvocation invocation = ToolInvocation.of(
                            step.action().name(),
                            step.parameters()
                        );
                        ToolResult result = toolExecutor.execute(invocation);
                        context = context
                            .withVariable("toolResult_" + step.id(), result)
                            .withArtifact(TextArtifact.of(
                                result.success() ? result.result().toString() : result.error(),
                                "tool-" + step.id()
                            ));
                        publishEvent(context, new DomainEvent.ToolCompleted(
                            step.id(),
                            step.action().name(),
                            result.success() ? List.of() : List.of(),
                            result.durationMs()
                        ));
                    }
                }
            }
        }
        return context;
    }
    
    private ExecutionContext memoryPhase(ExecutionContext context) throws Exception {
        if (memoryProvider != null) {
            log.debug("Memory phase: {}", context.id().asString());
            // Save the interaction to memory
            MemoryRecord record = MemoryRecord.of(
                "execution:" + context.id().asString(),
                context.getVariable("input", InputResult.class) != null
                    ? context.getVariable("input", InputResult.class).content()
                    : "No input",
                "execution"
            );
            memoryProvider.save(record);
            publishEvent(context, new DomainEvent.MemoryStored(
                "execution",
                "execution:" + context.id().asString(),
                record.value().length()
            ));
        }
        return context;
    }
    
    private ExecutionContext evaluationPhase(ExecutionContext context) throws Exception {
        if (evaluator != null) {
            log.debug("Evaluation phase: {}", context.id().asString());
            List<Artifact> outputs = context.artifacts();
            Evaluation evaluation = evaluator.evaluate(context);
            context = context
                .withVariable("evaluation", evaluation)
                .withAttribute("evaluationScore", evaluation.score());
            if (evaluation.status() == EvaluationStatus.FAILED) {
                log.warn("Evaluation failed: {}", evaluation.feedback());
            }
            publishEvent(context, new DomainEvent.EvaluationCompleted(
                evaluation.id(),
                evaluation.score(),
                evaluation.status().name()
            ));
        }
        return context;
    }
    
    private ExecutionContext outputPhase(ExecutionContext context) throws Exception {
        if (outputProvider != null) {
            log.debug("Output phase: {}", context.id().asString());
            OutputResult output = OutputResult.of(
                context.getVariable("completionResult", CompletionResult.class) != null
                    ? context.getVariable("completionResult", CompletionResult.class).getContent()
                    : "No output",
                OutputType.TEXT
            );
            outputProvider.send(output);
            publishEvent(context, new DomainEvent.OutputSent(
                output.id(),
                output.type().name(),
                output.content()
            ));
        }
        return context;
    }
    
    private CompletionRequest buildCompletionRequest(ExecutionContext context) {
        // Build request from context
        CompletionRequest.Builder builder = CompletionRequest.builder();
        
        // Get plan and reasoning
        Plan plan = context.getVariable("plan", Plan.class);
        ReasoningResult reasoning = context.getVariable("reasoningResult", ReasoningResult.class);
        InputResult input = context.getVariable("input", InputResult.class);
        
        // Build messages
        List<Message> messages = new ArrayList<>();
        messages.add(Message.system("You are a helpful assistant. " +
            (plan != null ? "Follow this plan: " + plan.name() : "") +
            (reasoning != null ? "Reasoning: " + reasoning.conclusion() : "")));
        
        if (input != null) {
            messages.add(Message.user(input.content()));
        }
        
        builder.messages(messages);
        
        // Add parameters from context
        Object temp = context.getAttribute("temperature");
        if (temp instanceof Double) {
            builder.parameter("temperature", temp);
        }
        Object maxTokens = context.getAttribute("maxTokens");
        if (maxTokens instanceof Integer) {
            builder.parameter("max_tokens", maxTokens);
        }
        
        return builder.build();
    }
    
    private void publishEvent(ExecutionContext context, EventPayload payload) {
        if (eventService != null) {
            Event event = DomainEvent.create(
                context.correlationId(),
                context.id().value(),
                context.principal(),
                payload
            );
            try {
                eventService.publish(event);
            } catch (Exception e) {
                log.warn("Failed to publish event: {}", e.getMessage());
            }
        }
    }
    
    private void publishCompletionEvent(ExecutionContext context) {
        publishEvent(context, new DomainEvent.ExecutionCompleted(
            context.id().asString(),
            context.artifacts(),
            Instant.now().toEpochMilli() - context.startedAt().toEpochMilli()
        ));
    }
    
    private void publishFailureEvent(ExecutionContext context, Exception e) {
        publishEvent(context, new DomainEvent.ExecutionFailed(
            context.id().asString(),
            e.getMessage()
        ));
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private InputProvider inputProvider;
        private ContextProvider contextProvider;
        private Planner planner;
        private Reasoner reasoner;
        private InferenceProvider inferenceProvider;
        private ToolExecutor toolExecutor;
        private MemoryProvider memoryProvider;
        private WorkflowEngine workflowEngine;
        private Evaluator evaluator;
        private GuardrailProvider guardrailProvider;
        private OutputProvider outputProvider;
        private EventService eventService;
        private ExecutorService executorService;
        
        public Builder inputProvider(InputProvider inputProvider) {
            this.inputProvider = inputProvider;
            return this;
        }
        
        public Builder contextProvider(ContextProvider contextProvider) {
            this.contextProvider = contextProvider;
            return this;
        }
        
        public Builder planner(Planner planner) {
            this.planner = planner;
            return this;
        }
        
        public Builder reasoner(Reasoner reasoner) {
            this.reasoner = reasoner;
            return this;
        }
        
        public Builder inferenceProvider(InferenceProvider inferenceProvider) {
            this.inferenceProvider = inferenceProvider;
            return this;
        }
        
        public Builder toolExecutor(ToolExecutor toolExecutor) {
            this.toolExecutor = toolExecutor;
            return this;
        }
        
        public Builder memoryProvider(MemoryProvider memoryProvider) {
            this.memoryProvider = memoryProvider;
            return this;
        }
        
        public Builder workflowEngine(WorkflowEngine workflowEngine) {
            this.workflowEngine = workflowEngine;
            return this;
        }
        
        public Builder evaluator(Evaluator evaluator) {
            this.evaluator = evaluator;
            return this;
        }
        
        public Builder guardrailProvider(GuardrailProvider guardrailProvider) {
            this.guardrailProvider = guardrailProvider;
            return this;
        }
        
        public Builder outputProvider(OutputProvider outputProvider) {
            this.outputProvider = outputProvider;
            return this;
        }
        
        public Builder eventService(EventService eventService) {
            this.eventService = eventService;
            return this;
        }
        
        public Builder executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }
        
        public AgentPipeline build() {
            return new AgentPipeline(this);
        }
    }
}