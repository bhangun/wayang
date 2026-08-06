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


import java.util.ArrayList;
import java.util.List;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.core.Permission;

/**
 * Role
 */
public record Role(
    String id,
    String name,
    String description,
    List<Permission> permissions,
    boolean systemRole
) {
    public static RoleBuilder builder() {
        return new RoleBuilder();
    }
    
    public static class RoleBuilder {
        private String id;
        private String name;
        private String description;
        private final List<Permission> permissions = new ArrayList<>();
        private boolean systemRole;
        
        public RoleBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public RoleBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public RoleBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public RoleBuilder permission(Permission permission) {
            this.permissions.add(permission);
            return this;
        }
        
        public RoleBuilder systemRole(boolean systemRole) {
            this.systemRole = systemRole;
            return this;
        }
        
        public Role build() {
            if (id == null) {
                id = Id.random().asString();
            }
            return new Role(id, name, description, permissions, systemRole);
        }
    }
}