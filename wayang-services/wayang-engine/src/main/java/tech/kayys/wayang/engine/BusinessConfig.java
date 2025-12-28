package tech.kayys.wayang.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business automation configuration.
 */
public class BusinessConfig {
    private List<String> approvalChain = new ArrayList<>();
    private Integer slaHours;
    private EscalationPolicy escalationPolicy = EscalationPolicy.NOTIFY_SUPERVISOR;
    private String formTemplate;
    private List<String> notificationChannels = new ArrayList<>();
    private Map<String, Object> businessRules = new HashMap<>();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BusinessConfig config = new BusinessConfig();

        public Builder approvalChain(List<String> chain) {
            config.approvalChain = chain;
            return this;
        }

        public Builder slaHours(int hours) {
            config.slaHours = hours;
            return this;
        }

        public Builder escalationPolicy(EscalationPolicy policy) {
            config.escalationPolicy = policy;
            return this;
        }

        public Builder formTemplate(String template) {
            config.formTemplate = template;
            return this;
        }

        public Builder notificationChannels(List<String> channels) {
            config.notificationChannels = channels;
            return this;
        }

        public Builder businessRule(String key, Object value) {
            config.businessRules.put(key, value);
            return this;
        }

        public BusinessConfig build() {
            return config;
        }
    }

    // Getters and setters
    public List<String> getApprovalChain() {
        return approvalChain;
    }

    public void setApprovalChain(List<String> chain) {
        this.approvalChain = chain;
    }

    public Integer getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(Integer hours) {
        this.slaHours = hours;
    }

    public EscalationPolicy getEscalationPolicy() {
        return escalationPolicy;
    }

    public void setEscalationPolicy(EscalationPolicy policy) {
        this.escalationPolicy = policy;
    }

    public String getFormTemplate() {
        return formTemplate;
    }

    public void setFormTemplate(String template) {
        this.formTemplate = template;
    }

    public List<String> getNotificationChannels() {
        return notificationChannels;
    }

    public void setNotificationChannels(List<String> channels) {
        this.notificationChannels = channels;
    }

    public Map<String, Object> getBusinessRules() {
        return businessRules;
    }

    public void setBusinessRules(Map<String, Object> rules) {
        this.businessRules = rules;
    }
}
