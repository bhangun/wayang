package tech.kayys.wayang.models.cache;

import lombok.Builder;
import lombok.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Cache key for model responses.
 * Includes tenant, model, and request fingerprint.
 */
@Value
@Builder
public class CacheKey {
    String tenantId;
    String modelId;
    String requestHash;
    
    /**
     * Generate cache key string.
     */
    public String toKey() {
        return String.format("%s:%s:%s", tenantId, modelId, requestHash);
    }
    
    /**
     * Create hash from request content.
     */
    public static String hashRequest(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}