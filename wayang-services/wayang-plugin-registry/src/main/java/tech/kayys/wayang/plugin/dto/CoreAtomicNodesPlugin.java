package tech.kayys.wayang.plugin.dto;

@Plugin(id = "core-atomic-nodes", type = PluginType.CORE)
public class CoreAtomicNodesPlugin {

    @NodeType("http-request")
    public class HttpRequestNode implements AtomicNode {
        @Override
        public Uni<NodeExecutionResult> execute(ExecutionContext context) {
            HttpRequestConfig config = context.getConfig();
            return webClient.request(config)
                    .map(response -> NodeExecutionResult.success(response.body()));
        }
    }

    @NodeType("condition")
    public class ConditionNode implements AtomicNode {
        @Override
        public Uni<NodeExecutionResult> execute(ExecutionContext context) {
            Expression condition = context.getConfig().getCondition();
            boolean result = expressionEvaluator.evaluate(condition, context.getData());
            return Uni.createFrom().item(
                    NodeExecutionResult.success(Map.of("result", result)));
        }
    }

    @NodeType("delay")
    public class DelayNode implements AtomicNode {
        @Override
        public Uni<NodeExecutionResult> execute(ExecutionContext context) {
            Duration delay = context.getConfig().getDuration();
            return Uni.createFrom().item(context.getData())
                    .onItem().delayIt().by(delay)
                    .map(data -> NodeExecutionResult.success(data));
        }
    }
}
