package tech.kayys.wayang.inference;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Iterator;

/**
 * Completion Stream
 */
public interface CompletionStream extends Iterator<CompletionResult> {
    void close() throws Exception;
    boolean isComplete();
    String getStreamId();
}