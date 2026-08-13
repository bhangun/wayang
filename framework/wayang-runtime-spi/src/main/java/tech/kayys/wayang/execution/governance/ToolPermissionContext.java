package tech.kayys.wayang.execution.governance;

import java.util.List;

/**
 * Immutable snapshot of who is invoking a tool and in what execution context.
 * Passed to every {@link ToolPolicy} for evaluation (§31 — Permission Engine).
 *
 * @param tenantId    The tenant namespace. Null in standalone/dev mode.
 * @param userId      The authenticated user. Null in programmatic/API mode.
 * @param executionId The parent execution ID.
 * @param roles       Roles granted to this principal (e.g. "admin", "readonly", "approved").
 * @param toolName    The tool being invoked.
 * @param level       The capability level required by the tool.
 */
public record ToolPermissionContext(
        String tenantId,
        String userId,
        String executionId,
        List<String> roles,
        String toolName,
        ToolCapabilityLevel level
) {
    public ToolPermissionContext {
        roles = roles != null ? List.copyOf(roles) : List.of();
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isAnonymous() {
        return userId == null || userId.isBlank();
    }

    /** Convenience: standalone/dev context with no tenant or user. */
    public static ToolPermissionContext standalone(String executionId, String toolName, ToolCapabilityLevel level) {
        return new ToolPermissionContext(null, null, executionId, List.of("*"), toolName, level);
    }
}
