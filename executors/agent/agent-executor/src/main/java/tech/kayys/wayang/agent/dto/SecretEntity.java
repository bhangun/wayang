package tech.kayys.wayang.agent.dto;

public class SecretEntity {
    private String key;
    private String encryptedValue;
    private String tenantId;

    public SecretEntity() {
    }

    public SecretEntity(String key, String encryptedValue, String tenantId) {
        this.key = key;
        this.encryptedValue = encryptedValue;
        this.tenantId = tenantId;
    }

    public String getKey() {
        return key;
    }

    public String getEncryptedValue() {
        return encryptedValue;
    }

    public String getTenantId() {
        return tenantId;
    }
}
