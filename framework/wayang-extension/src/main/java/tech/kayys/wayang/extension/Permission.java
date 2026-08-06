package tech.kayys.wayang.core;
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
 * Represents a permission or permission requirement.
 */
public record Permission(
    PermissionType type,
    String resource,
    String action
) {
    
    public static Permission of(PermissionType type, String resource, String action) {
        return new Permission(type, resource, action);
    }
    
    public static Permission of(String resource, String action) {
        return new Permission(PermissionType.REQUIRED, resource, action);
    }
    
    public Permission withType(PermissionType type) {
        return new Permission(type, resource, action);
    }
    
    @Override
    public String toString() {
        return type + ":" + resource + ":" + action;
    }
}

