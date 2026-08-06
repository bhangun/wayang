package tech.kayys.wayang.engine;
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
 * Phase Type
 */
public enum PhaseType {
    GUARD(0),
    INPUT(10),
    CONTEXT(20),
    PLANNING(30),
    REASONING(40),
    INFERENCE(50),
    TOOL(60),
    MEMORY(70),
    EVALUATION(80),
    OUTPUT(90),
    COMPLETE(100),
    CUSTOM(999);
    
    private final int order;
    
    PhaseType(int order) {
        this.order = order;
    }
    
    public int order() {
        return order;
    }
}
