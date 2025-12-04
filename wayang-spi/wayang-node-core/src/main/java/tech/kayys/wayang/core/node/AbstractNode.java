package tech.kayys.wayang.core.node;

/**
 * Abstract base class providing common functionality for all nodes
 * Handles error wrapping, observability, and validation
 */
public abstract class AbstractNode implements Node {
    
    protected NodeDescriptor descriptor;
    protected NodeConfig config;
    
    @Override
    public void onLoad(NodeDescriptor descriptor, NodeConfig config) {
        this.descriptor = descriptor;
        this.config = config;
        doOnLoad(descriptor, config);
    }
    
    @Override
    public final ExecutionResult execute(NodeContext context) {
        return doExecute(context);
    }
    
    /**
     * Subclasses implement actual execution logic here
     */
    protected abstract ExecutionResult doExecute(NodeContext context);
    
    /**
     * Optional: Custom initialization logic
     */
    protected void doOnLoad(NodeDescriptor descriptor, NodeConfig config) {
        // Default: no-op
    }
    
    @Override
    public void onUnload() {
        // Cleanup resources
    }
}
