package tech.kayys.wayang.core.factory;

import tech.kayys.wayang.core.node.Node;
import tech.kayys.wayang.core.node.NodeDescriptor;

public class PluginNodeFactory implements NodeFactory {
    
    @Override
    public Node createNode(NodeDescriptor descriptor) throws Exception {
        // Simple implementation for now
        return null;
    }
    
    @Override
    public boolean supports(NodeDescriptor descriptor) {
        return true;
    }
}
