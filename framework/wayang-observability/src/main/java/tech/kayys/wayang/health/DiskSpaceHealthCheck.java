package tech.kayys.wayang.health;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Built-in Health Checks
 */
public class DiskSpaceHealthCheck implements HealthCheck {
    private final Path path;
    private final long thresholdBytes;
    
    public DiskSpaceHealthCheck(Path path, long thresholdBytes) {
        this.path = path;
        this.thresholdBytes = thresholdBytes;
    }
    
    @Override
    public String name() {
        return "disk-space";
    }
    
    @Override
    public HealthResult check() throws Exception {
        long free = Files.getFileStore(path).getUsableSpace();
        if (free < thresholdBytes) {
            return HealthResult.degraded("Low disk space: " + (free / 1024 / 1024) + " MB free");
        }
        return HealthResult.healthy();
    }
}
