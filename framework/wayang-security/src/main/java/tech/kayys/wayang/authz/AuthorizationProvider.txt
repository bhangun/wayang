package tech.kayys.wayang.authz;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.core.Permission;
import tech.kayys.wayang.core.Principal;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.resource.Resource;
import tech.kayys.wayang.spi.security.AuthorizationResult;


/**
 * Authorization Provider - authorizes access.
 */
public interface AuthorizationProvider extends Extension {
    
    /**
     * Authorize access
     */
    AuthorizationResult authorize(AuthorizationRequest request) throws Exception;
    
    /**
     * Authorize with principal and resource
     */
    default AuthorizationResult authorize(Principal principal, Resource resource, String action) throws Exception {
        return authorize(new AuthorizationRequest(principal, resource, action, Map.of()));
    }
    
    /**
     * Check if has permission
     */
    default boolean hasPermission(Principal principal, Permission permission) throws Exception {
        return authorize(new AuthorizationRequest(principal, null, null, 
            Map.of("permission", permission))).authorized();
    }
    
    /**
     * Get permissions for principal
     */
    default Set<Permission> getPermissions(Principal principal) throws Exception {
        return Set.of();
    }
}