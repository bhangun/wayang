package tech.kayys.wayang.auth;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.core.Permission;
import tech.kayys.wayang.core.Principal;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.security.AuthorizationResult;

public class DefaultAuthorizationProvider implements AuthorizationProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, Set<Permission>> permissions = new ConcurrentHashMap<>();
    
    public DefaultAuthorizationProvider() {
        this.id = Id.random().asString();
        this.name = "default-authz-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Authorization Provider")
            .version(version)
            .label("type", "authz")
            .now()
            .build();
        
        // Default permissions
        permissions.put("admin", Set.of(
            Permission.of("*:*", "*")
        ));
        permissions.put("user", Set.of(
            Permission.of("agent:*", "view"),
            Permission.of("execution:*", "execute")
        ));
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("authz"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public AuthorizationResult authorize(AuthorizationRequest request) throws Exception {
        Principal principal = request.principal();
        if (principal == null) {
            return AuthorizationResult.failure("No principal provided");
        }
        
        // Check if principal has roles
        Object rolesObj = principal.getAttribute("roles");
        Set<String> roles = new HashSet<>();
        if (rolesObj instanceof List) {
            roles.addAll((List<String>) rolesObj);
        }
        
        // Check each role's permissions
        for (String role : roles) {
            Set<Permission> rolePermissions = permissions.get(role);
            if (rolePermissions != null) {
                for (Permission permission : rolePermissions) {
                    if (matches(permission, request)) {
                        return AuthorizationResult.success();
                    }
                }
            }
        }
        
        return AuthorizationResult.failure("Permission denied", Set.of());
    }
    
    private boolean matches(Permission permission, AuthorizationRequest request) {
        String resource = permission.resource();
        String action = permission.action();
        
        // Wildcard check
        if ("*:*".equals(resource) && "*".equals(action)) {
            return true;
        }
        
        if (request.resource() != null) {
            String resourceType = request.resource().type().asString();
            if (resource.equals(resourceType + ":*") || resource.equals("*:*")) {
                return true;
            }
        }
        
        return false;
    }
}

