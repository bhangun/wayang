package tech.kayys.wayang.authz;
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

import tech.kayys.wayang.core.Principal;
import tech.kayys.wayang.resource.Resource;

/**
 * Authorization Request
 */
public record AuthorizationRequest(
    Principal principal,
    Resource resource,
    String action,
    Map<String, Object> context
) {
    public static AuthorizationRequest of(Principal principal, Resource resource, String action) {
        return new AuthorizationRequest(principal, resource, action, Map.of());
    }
}
