package tech.kayys.wayang.node.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Custom Node Type Descriptor.
 * 
 * Extends NodeTypeDescriptor with plugin-specific fields.
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomNodeTypeDescriptor extends NodeTypeDescriptor {

    private String pluginId;
    private String implementationClass;
    private SecurityPolicy securityPolicy;
    private String createdBy;
    private String createdAt;
    private String updatedAt;

    public CustomNodeTypeDescriptor() {
        super();
    }

    /**
     * Security policy for custom nodes.
     */
    public static class SecurityPolicy {
        private boolean requiresApproval;
        private boolean sandboxed;
        private String[] allowedCapabilities;
        private String[] deniedCapabilities;

        public SecurityPolicy() {
        }

        // Getters and setters
        public boolean isRequiresApproval() {
            return requiresApproval;
        }

        public void setRequiresApproval(boolean requiresApproval) {
            this.requiresApproval = requiresApproval;
        }

        public boolean isSandboxed() {
            return sandboxed;
        }

        public void setSandboxed(boolean sandboxed) {
            this.sandboxed = sandboxed;
        }

        public String[] getAllowedCapabilities() {
            return allowedCapabilities;
        }

        public void setAllowedCapabilities(String[] allowedCapabilities) {
            this.allowedCapabilities = allowedCapabilities;
        }

        public String[] getDeniedCapabilities() {
            return deniedCapabilities;
        }

        public void setDeniedCapabilities(String[] deniedCapabilities) {
            this.deniedCapabilities = deniedCapabilities;
        }
    }

    // Getters and setters
    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    public void setSecurityPolicy(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
