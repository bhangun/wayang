package tech.kayys.wayang.plugin.example;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.plugin.CommunicationProtocol;
import tech.kayys.wayang.plugin.ControlPlaneExecutorRegistry;
import tech.kayys.wayang.plugin.ControlPlaneNodeRegistry;
import tech.kayys.wayang.plugin.ControlPlaneWidgetRegistry;
import tech.kayys.wayang.plugin.SchemaValidator;
import tech.kayys.wayang.plugin.UIReference;
import tech.kayys.wayang.plugin.execution.ExecutionContext;
import tech.kayys.wayang.plugin.execution.ExecutionContract;
import tech.kayys.wayang.plugin.execution.ExecutionContractBuilder;
import tech.kayys.wayang.plugin.execution.ExecutionMode;
import tech.kayys.wayang.plugin.executor.ExecutorBinding;
import tech.kayys.wayang.plugin.executor.ExecutorRegistration;
import tech.kayys.wayang.plugin.node.NodeDefinition;

/**
 * Example: How Control Plane uses these components
 */
@ApplicationScoped
public class ControlPlaneExample {

  @Inject
  ControlPlaneNodeRegistry nodeRegistry;

  @Inject
  ControlPlaneExecutorRegistry executorRegistry;

  @Inject
  ControlPlaneWidgetRegistry widgetRegistry;

  @Inject
  SchemaValidator schemaValidator;

  @Inject
  ExecutionContractBuilder contractBuilder;

  /**
   * Register a complete node (called by plugin installer)
   */
  public void registerNode() {
    // Create JSON schemas
    String configSchemaJson = """
        {
          "type": "object",
          "properties": {
            "threshold": {
              "type": "number",
              "minimum": 0.0,
              "maximum": 1.0
            },
            "model": {
              "type": "string",
              "enum": ["default", "advanced", "fast"]
            }
          },
          "required": ["model"]
        }
        """;

    String inputSchemaJson = """
        {
          "type": "object",
          "properties": {
            "text": {
              "type": "string",
              "minLength": 1,
              "maxLength": 10000
            }
          },
          "required": ["text"]
        }
        """;

    // Create node definition
    NodeDefinition node = new NodeDefinition();
    node.type = "sentiment.analyzer";
    node.label = "Sentiment Analyzer";
    node.category = "AI";
    node.version = "1.0.0";

    // JSON Schemas
    node.configSchema = schemaValidator.createSchema(configSchemaJson);
    node.inputSchema = schemaValidator.createSchema(inputSchemaJson);

    // Executor binding (indirect)
    node.executorBinding = new ExecutorBinding(
        "executor.sentiment.analyzer",
        ExecutionMode.SYNC,
        CommunicationProtocol.GRPC);

    // UI reference (not implementation)
    node.uiReference = new UIReference("widget.ai.sentiment");

    // Register
    nodeRegistry.register(node);
  }

  /**
   * Register executor (called by executor on startup)
   */
  public Uni<Void> registerExecutor() {
    ExecutorRegistration registration = new ExecutorRegistration();
    registration.executorId = "executor.sentiment.analyzer";
    registration.executorType = "ai";
    registration.endpoint = URI.create("http://sentiment-executor:8080/execute");
    registration.protocol = CommunicationProtocol.REST;
    registration.capabilities.add("nlp");
    registration.capabilities.add("sentiment-analysis");
    registration.supportedNodes.add("sentiment.analyzer");
    registration.metadata.language = "java";
    registration.metadata.maxConcurrency = 50;
    registration.metadata.timeoutMs = 30000;
    registration.registeredAt = Instant.now();

    return executorRegistry.register(registration);
  }

  /**
   * Execute node (called by Workflow Engine)
   */
  public Uni<ExecutionContract> executeNode(
      String workflowRunId,
      String nodeInstanceId,
      Map<String, Object> inputs,
      Map<String, Object> config) {

    ExecutionContext context = new ExecutionContext();
    context.variables.put("workflowRunId", workflowRunId);

    return contractBuilder.build(
        workflowRunId,
        "sentiment.analyzer",
        nodeInstanceId,
        inputs,
        config,
        context);
  }
}