package tech.kayys.wayang.security;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Set;

import tech.kayys.wayang.core.Permission;

/**
 * Authorization Result
 */
public record AuthorizationResult(
    boolean authorized,
    String message,
    Set<Permission> requiredPermissions,
    Set<Permission> grantedPermissions
) {
    public static AuthorizationResult success() {
        return new AuthorizationResult(true, null, Set.of(), Set.of());
    }
    
    public static AuthorizationResult failure(String message) {
        return new AuthorizationResult(false, message, Set.of(), Set.of());
    }
    
    public static AuthorizationResult failure(String message, Set<Permission> required) {
        return new AuthorizationResult(false, message, required, Set.of());
    }
}
