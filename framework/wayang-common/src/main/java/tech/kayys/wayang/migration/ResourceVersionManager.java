package tech.kayys.wayang.migration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;

import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Resource Version Manager
 */
public interface ResourceVersionManager {
    
    Version getCurrentVersion(Resource resource);
    
    boolean isLatestVersion(Resource resource);
    
    Resource upgrade(Resource resource) throws Exception;
    
    Resource downgrade(Resource resource, Version target) throws Exception;
    
    List<Version> getAvailableVersions(ResourceType type);
}