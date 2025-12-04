package tech.kayys.wayang.core.factory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.core.node.Node;
import tech.kayys.wayang.core.node.NodeDescriptor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class NodeRegistry {
    
    private final Map<String, NodeFactory> factories = new ConcurrentHashMap<>();
    private final Map<String, NodeDescriptor> descriptors = new ConcurrentHashMap<>();
    
    public void registerFactory(String type, NodeFactory factory) {
        factories.put(type, factory);
    }
    
    public Node createNode(String type, NodeDescriptor descriptor) throws Exception {
        NodeFactory factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("No factory found for type: " + type);
        }
        return factory.createNode(descriptor);
    }
}
