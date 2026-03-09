package tech.kayys.wayang.eip.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
@Executor(executorType = "eip.debug-print", maxConcurrentTasks = 100, supportedNodeTypes = {
    "debug-print", "debug"
}, version = "1.0.0")
public class DebugPrintExecutor extends AbstractWorkflowExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(DebugPrintExecutor.class);

  @Override
  public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
    Map<String, Object> context = task.context();
    String prefix = valueAsString(context.getOrDefault("prefix", "EIP DEBUG"));
    String level = valueAsString(context.getOrDefault("level", "info")).toLowerCase();
    Object payload = context.get("message");
    if (payload == null) {
      payload = context.get("payload");
    }
    if (payload == null) {
      payload = context;
    }

    String rendered = payload == null ? "null" : String.valueOf(payload);
    log(level, prefix, rendered);

    Map<String, Object> output = new LinkedHashMap<>();
    output.put("message", payload);
    output.put("debugPrinted", true);
    output.put("debugLevel", level);
    output.put("debugPrefix", prefix);
    output.put("debugPrintedAt", Instant.now().toString());

    return Uni.createFrom().item(SimpleNodeExecutionResult.success(
        task.runId(),
        task.nodeId(),
        task.attempt(),
        output,
        task.token(),
        java.time.Duration.ZERO));
  }

  private static String valueAsString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private void log(String level, String prefix, String rendered) {
    String message = prefix + " | " + rendered;
    switch (level) {
      case "trace":
        LOG.trace(message);
        return;
      case "debug":
        LOG.debug(message);
        return;
      case "warn":
      case "warning":
        LOG.warn(message);
        return;
      case "error":
        LOG.error(message);
        return;
      default:
        LOG.info(message);
    }
  }
}
