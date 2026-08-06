package tech.kayys.wayang.spi.output;
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
 * Streaming Output
 */
public interface StreamingOutput {
    boolean hasNext();
    OutputResult next() throws Exception;
    void close() throws Exception;
    long getTotalBytes();
    long getProgress();
}

