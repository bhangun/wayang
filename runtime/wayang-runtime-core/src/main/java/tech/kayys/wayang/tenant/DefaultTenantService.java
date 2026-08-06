package tech.kayys.wayang.tenant;


import java.util.*;
import java.util.concurrent.*;

import tech.kayys.wayang.core.Principal;

/**
 * Default Tenant Service Implementation
 */
public class DefaultTenantService implements TenantService {
    
    private final Map<String, Tenant> tenants = new ConcurrentHashMap<>();
    private final Map<String, List<String>> tenantUsers = new ConcurrentHashMap<>();
    private final Tenant defaultTenant;
    
    public DefaultTenantService() {
        this.defaultTenant = Tenant.builder()
            .id("default")
            .name("Default Tenant")
            .status(TenantStatus.ACTIVE)
            .build();
        tenants.put("default", defaultTenant);
    }
    
    @Override
    public Tenant getTenant(String tenantId) {
        Tenant tenant = tenants.get(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }
        return tenant;
    }
    
    @Override
    public Tenant getTenantForRequest(Principal principal) {
        if (principal == null) {
            return defaultTenant;
        }
        
        // Check if principal has tenant info
        String tenantId = principal.getAttribute("tenantId");
        if (tenantId != null && tenants.containsKey(tenantId)) {
            return tenants.get(tenantId);
        }
        
        return defaultTenant;
    }
    
    @Override
    public void createTenant(Tenant tenant) {
        if (tenants.containsKey(tenant.id())) {
            throw new IllegalArgumentException("Tenant already exists: " + tenant.id());
        }
        tenants.put(tenant.id(), tenant);
    }
    
    @Override
    public void updateTenant(Tenant tenant) {
        if (!tenants.containsKey(tenant.id())) {
            throw new IllegalArgumentException("Tenant not found: " + tenant.id());
        }
        tenants.put(tenant.id(), tenant);
    }
    
    @Override
    public void deleteTenant(String tenantId) {
        if ("default".equals(tenantId)) {
            throw new IllegalArgumentException("Cannot delete default tenant");
        }
        tenants.remove(tenantId);
    }
    
    @Override
    public List<Tenant> listTenants() {
        return new ArrayList<>(tenants.values());
    }
    
    @Override
    public boolean tenantExists(String tenantId) {
        return tenants.containsKey(tenantId);
    }
    
    public void addUserToTenant(String tenantId, String userId) {
        tenantUsers.computeIfAbsent(tenantId, k -> new CopyOnWriteArrayList<>()).add(userId);
    }
    
    public void removeUserFromTenant(String tenantId, String userId) {
        List<String> users = tenantUsers.get(tenantId);
        if (users != null) {
            users.remove(userId);
        }
    }
    
    public List<String> getUsersForTenant(String tenantId) {
        return tenantUsers.getOrDefault(tenantId, List.of());
    }
    
    public boolean isUserInTenant(String tenantId, String userId) {
        List<String> users = tenantUsers.get(tenantId);
        return users != null && users.contains(userId);
    }
}
