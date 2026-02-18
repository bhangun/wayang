package tech.kayys.wayang.agent.dto;

import java.util.List;

public class ApiKeyEntity {
    private String id;
    private String userId;
    private String key;
    private String tenantId;
    private boolean active;
    private List<String> roles;
    private List<String> permissions;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isActive() {
        return active;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}