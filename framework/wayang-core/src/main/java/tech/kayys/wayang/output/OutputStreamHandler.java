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
 * Output Stream Handler
 */
@FunctionalInterface
public interface OutputStreamHandler {
    void onChunk(OutputResult chunk) throws Exception;
}

