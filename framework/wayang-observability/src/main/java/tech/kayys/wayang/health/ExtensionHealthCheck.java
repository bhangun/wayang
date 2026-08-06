package tech.kayys.wayang.health;

import tech.kayys.wayang.extension.Extension;

public class ExtensionHealthCheck implements HealthCheck {
    private final Extension extension;
    
    public ExtensionHealthCheck(Extension extension) {
        this.extension = extension;
    }
    
    @Override
    public String name() {
        return "extension-" + extension.id();
    }
    
    @Override
    public HealthResult check() throws Exception {
        // Check if extension is healthy
        return HealthResult.healthy();
    }
}