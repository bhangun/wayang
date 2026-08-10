package tech.kayys.wayang.health;

import java.net.URL;
import java.net.URLConnection;

public class ConnectionHealthCheck implements HealthCheck {
    private final String url;
    private final int timeoutMs;
    
    public ConnectionHealthCheck(String url, int timeoutMs) {
        this.url = url;
        this.timeoutMs = timeoutMs;
    }
    
    @Override
    public String name() {
        return "connection-" + url.replaceAll("[^a-zA-Z0-9]", "-");
    }
    
    @Override
    public HealthResult check() throws Exception {
        try {
            long start = System.currentTimeMillis();
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(timeoutMs);
            connection.connect();
            long duration = System.currentTimeMillis() - start;
            return HealthResult.healthy();
        } catch (Exception e) {
            return HealthResult.unhealthy("Connection failed: " + e.getMessage());
        }
    }
}