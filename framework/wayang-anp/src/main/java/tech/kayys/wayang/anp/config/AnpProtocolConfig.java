package tech.kayys.wayang.anp.config;

import java.util.Map;
import java.util.Objects;

/**
 * Configuration for the Wayang ANP protocol adapter.
 *
 * <p>By default, {@code enabled} is {@code false} so Wayang remains 100% in-house / local
 * until explicitly activated.
 */
public record AnpProtocolConfig(
        boolean enabled,
        String domain,
        int port,
        String defaultKeyId,
        boolean preferA2A,
        boolean requireSignature,
        Map<String, String> properties
) {
    public AnpProtocolConfig {
        Objects.requireNonNull(domain, "domain");
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    /** Creates disabled default config (local-first). */
    public static AnpProtocolConfig disabled() {
        return new AnpProtocolConfig(false, "localhost", 8080, "key-1", true, false, Map.of());
    }

    /** Creates an enabled configuration for the specified domain. */
    public static AnpProtocolConfig enabled(String domain) {
        return new AnpProtocolConfig(true, domain, 443, "key-1", true, true, Map.of());
    }

    /** Creates an enabled configuration for testing. */
    public static AnpProtocolConfig testConfig() {
        return new AnpProtocolConfig(true, "test.wayang.local", 8443, "key-test", true, false, Map.of());
    }
}
