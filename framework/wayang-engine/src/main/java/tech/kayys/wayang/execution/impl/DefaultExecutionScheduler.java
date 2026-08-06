package tech.kayys.wayang.execution.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import tech.kayys.wayang.execution.ExecutionEdge;
import tech.kayys.wayang.execution.ExecutionGraph;
import tech.kayys.wayang.execution.ExecutionNode;
import tech.kayys.wayang.execution.ExecutionScheduler;
import tech.kayys.wayang.execution.ExecutionState;
import tech.kayys.wayang.execution.NodeResult;
import tech.kayys.wayang.execution.NodeStatus;
import tech.kayys.wayang.sandbox.runtime.ResourceManager;

/**
 * Default implementation of the ExecutionScheduler.
 * 
 * <p>
 * Supports multiple scheduling strategies:
 * <ul>
 * <li>Sequential - Single-threaded execution</li>
 * <li>Parallel - Maximum parallelism</li>
 * <li>Priority - Priority-based execution</li>
 * <li>ResourceAware - Resource-constrained scheduling</li>
 * <li>CostAware - Cost-optimized scheduling</li>
 * <li>DeadlineAware - Deadline-constrained scheduling</li>
 * </ul>
 */
public class DefaultExecutionScheduler implements ExecutionScheduler {

    private final SchedulingStrategy strategy;
    private final Map<UUID, ExecutionState> executionStates;
    private final ResourceManager resourceManager;

    public DefaultExecutionScheduler(SchedulingStrategy strategy, ResourceManager resourceManager) {
        this.strategy = strategy != null ? strategy : new ParallelSchedulingStrategy();
        this.executionStates = new ConcurrentHashMap<>();
        this.resourceManager = resourceManager;
    }

    @Override
    public List<ExecutionNode> schedule(ExecutionGraph graph, ExecutionState state) {
        // Get all nodes that are ready for execution
        Set<ExecutionNode> readyNodes = getReadyNodes(graph, state);

        // Apply scheduling strategy
        List<ExecutionNode> scheduled = strategy.schedule(readyNodes, state);

        // Update state
        for (ExecutionNode node : scheduled) {
            state.updateNodeStatus(node.id(), NodeStatus.READY);
        }

        return scheduled;
    }

    @Override
    public List<ExecutionNode> getReadyNodes(ExecutionGraph graph, ExecutionState state) {
        // Get all nodes in CREATED state
        Set<ExecutionNode> ready = graph.nodes().stream()
                .filter(node -> node.status() == NodeStatus.CREATED)
                .filter(node -> areDependenciesSatisfied(node, state))
                .filter(node -> hasRequiredResources(node))
                .collect(Collectors.toSet());

        // Filter by scheduling constraints
        return ready.stream()
                .filter(node -> !isBlocked(node, state))
                .collect(Collectors.toList());
    }

    @Override
    public void updateState(ExecutionGraph graph, ExecutionState state, NodeResult result) {
        UUID nodeId = result.nodeId();
        state.updateNodeResult(nodeId, result);
        state.updateNodeStatus(nodeId, result.isSuccess() ? NodeStatus.COMPLETED : NodeStatus.FAILED);

        // Release resources
        if (resourceManager != null) {
            resourceManager.release(nodeId);
        }

        // Check if any waiting nodes should be scheduled
        for (ExecutionNode node : graph.nodes()) {
            if (node.status() == NodeStatus.WAITING) {
                // Check if wait condition is satisfied
                if (isWaitConditionSatisfied(node, state)) {
                    state.updateNodeStatus(node.id(), NodeStatus.CREATED);
                }
            }
        }
    }

    private boolean areDependenciesSatisfied(ExecutionNode node, ExecutionState state) {
        for (ExecutionEdge edge : node.incomingEdges()) {
            NodeResult sourceResult = state.getNodeResult(edge.from());
            if (sourceResult == null) {
                return false;
            }

            // Check edge condition
            if (!edge.condition().evaluate(state.getContext(), sourceResult)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRequiredResources(ExecutionNode node) {
        if (resourceManager == null) {
            return true;
        }
        return resourceManager.canAcquire(node.resourceRequirements());
    }

    private boolean isBlocked(ExecutionNode node, ExecutionState state) {
        // Check for deadlocks, cycles, or other blocking conditions
        return false;
    }

    private boolean isWaitConditionSatisfied(ExecutionNode node, ExecutionState state) {
        // Check if wait condition has been met
        return true;
    }
}