package tech.kayys.wayang.health;

public class MemoryHealthCheck implements HealthCheck {
    private final long maxMemoryPercentage;
    
    public MemoryHealthCheck(long maxMemoryPercentage) {
        this.maxMemoryPercentage = maxMemoryPercentage;
    }
    
    @Override
    public String name() {
        return "memory";
    }
    
    @Override
    public HealthResult check() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        double percentage = (double) used / max * 100;
        
        if (percentage > maxMemoryPercentage) {
            return HealthResult.degraded("High memory usage: " + String.format("%.1f", percentage) + "%");
        }
        return HealthResult.healthy();
    }
}
