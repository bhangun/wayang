package tech.kayys.wayang.event;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Map;

/**
 * Document type for Knowledge events
 */
public interface Document {
    String id();
    String content();
    Map<String, Object> metadata();
    default String getContent() { return content(); }
    default Map<String, Object> getMetadata() { return metadata(); }
}
