package tech.kayys.wayang.tenant;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Tenant Context - ThreadLocal for tenant propagation
 */
public class TenantContext {
    
    private static final ThreadLocal<Tenant> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> contextData = ThreadLocal.withInitial(HashMap::new);
    
    public static void setCurrentTenant(Tenant tenant) {
        if (tenant == null) {
            clear();
        } else {
            currentTenant.set(tenant);
        }
    }
    
    public static Tenant getCurrentTenant() {
        return currentTenant.get();
    }
    
    public static String getCurrentTenantId() {
        Tenant tenant = getCurrentTenant();
        return tenant != null ? tenant.id() : null;
    }
    
    public static void setData(String key, Object value) {
        contextData.get().put(key, value);
    }
    
    public static Object getData(String key) {
        return contextData.get().get(key);
    }
    
    public static void clearData() {
        contextData.get().clear();
    }
    
    public static void clear() {
        currentTenant.remove();
        contextData.remove();
    }
    
    /**
     * Run with tenant context
     */
    public static <T> T withTenant(Tenant tenant, Supplier<T> supplier) {
        Tenant previous = getCurrentTenant();
        try {
            setCurrentTenant(tenant);
            return supplier.get();
        } finally {
            if (previous != null) {
                setCurrentTenant(previous);
            } else {
                clear();
            }
        }
    }
    
    /**
     * Run with tenant context
     */
    public static <T> T withTenantId(String tenantId, TenantService tenantService, Supplier<T> supplier) {
        Tenant tenant = tenantService.getTenant(tenantId);
        return withTenant(tenant, supplier);
    }
}
