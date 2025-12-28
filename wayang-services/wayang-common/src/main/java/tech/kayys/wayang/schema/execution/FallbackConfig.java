package tech.kayys.wayang.schema.execution;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;
import java.util.List;

/**
 * FallbackConfig - Fallback configuration for error handling
 */
@RegisterForReflection
public class FallbackConfig {
    private String type = "none"; // node, static, none
    private String nodeId;
    private Object staticResponse;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        List<String> validTypes = Arrays.asList("node", "static", "none");
        if (!validTypes.contains(type)) {
            throw new IllegalArgumentException("Invalid fallback type: " + type);
        }
        this.type = type;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Object getStaticResponse() {
        return staticResponse;
    }

    public void setStaticResponse(Object staticResponse) {
        this.staticResponse = staticResponse;
    }
}
