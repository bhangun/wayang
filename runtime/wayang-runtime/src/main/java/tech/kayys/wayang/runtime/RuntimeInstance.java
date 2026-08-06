package tech.kayys.wayang.runtime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.time.Instant;
import java.util.*;

import tech.kayys.wayang.core.State;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.execution.ExecutionError;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.identity.ResourceId.ExecutionId;
import tech.kayys.wayang.resource.Artifact;
import tech.kayys.wayang.resource.Resource;

/**
 * Runtime Model - represents running instances of definitions.
 */
public sealed interface RuntimeInstance extends Resource {
    
    ResourceId definitionId();
    
    ExecutionId executionId();
    
    State currentState();
    
    Instant startedAt();
    
    Instant updatedAt();
    
    ExecutionContext context();
    
    List<Artifact> outputs();
    
    List<ExecutionError> errors();
}