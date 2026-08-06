package tech.kayys.wayang.reasoner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


/**
 * Reasoning Strategies
 */
public enum ReasoningStrategy {
    CHAIN_OF_THOUGHT,
    REACT,
    REFLEXION,
    SELF_CONSISTENCY,
    TREE_OF_THOUGHTS,
    GRAPH_OF_THOUGHTS,
    DEBATE,
    CONSTITUTIONAL_AI
}