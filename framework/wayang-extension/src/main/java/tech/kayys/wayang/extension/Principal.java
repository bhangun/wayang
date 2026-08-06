package tech.kayys.wayang.extension;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



import java.util.Collections;
import java.util.Map;


/**
 * Represents a user or service principal in the Wayang system.
 */
public record Principal(
    Id id,
    String username,
    String email,
    PrincipalType type,
    String tenant,
    String namespace,
    Map<String, String> attributes
) {
    
    public static Principal system() {
        return new Principal(
            Id.random(),
            "system",
            "system@wayang.io",
            PrincipalType.SYSTEM,
            null,
            null,
            Collections.emptyMap()
        );
    }
    
    public static Principal anonymous() {
        return new Principal(
            Id.random(),
            "anonymous",
            null,
            PrincipalType.ANONYMOUS,
            null,
            null,
            Collections.emptyMap()
        );
    }
    
    public boolean isSystem() {
        return type == PrincipalType.SYSTEM;
    }
    
    public boolean isAnonymous() {
        return type == PrincipalType.ANONYMOUS;
    }
    
    public boolean isUser() {
        return type == PrincipalType.USER;
    }
    
    public boolean isService() {
        return type == PrincipalType.SERVICE;
    }
    
    public boolean hasAttribute(String key) {
        return attributes != null && attributes.containsKey(key);
    }
    
    public String getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }
}