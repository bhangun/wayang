package tech.kayys.wayang.tenant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;

import tech.kayys.wayang.core.Principal;

/**
 * Tenant Management
 */
public interface TenantService {
    
    Tenant getTenant(String tenantId);
    
    Tenant getTenantForRequest(Principal principal);
    
    void createTenant(Tenant tenant);
    
    void updateTenant(Tenant tenant);
    
    void deleteTenant(String tenantId);
    
    List<Tenant> listTenants();
    
    boolean tenantExists(String tenantId);
}