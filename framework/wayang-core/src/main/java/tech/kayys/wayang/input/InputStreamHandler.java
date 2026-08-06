package tech.kayys.wayang.input;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import tech.kayys.wayang.agent.AgentRequest;

/**
 * Input Stream Handler
 */
@FunctionalInterface
public interface InputStreamHandler {
    void onChunk(AgentRequest chunk) throws Exception;
}
