package tech.kayys.wayang.cli.bootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GollekBootstrapService {

    public static void ensureRunning(String profile) {
        Path configYaml = Paths.get(System.getProperty("user.dir"), "config", "providers", "gollek.yaml");
        if (!configYaml.toFile().exists()) {
            configYaml = Paths.get(System.getProperty("user.dir"), "Families", "wayang", "config", "providers", "gollek.yaml");
        }
        
        GollekConfig config = GollekConfig.load(configYaml, profile);
        
        // If strategy is not grpc or embedded, we might not need to start it locally (e.g. REST remote)
        if ("embedded".equals(config.getStrategy())) {
            return; // Embedded runs in-process
        }
        
        if ("grpc".equals(config.getStrategy()) && "localhost".equals(config.getGrpcHost())) {
            
            boolean isRunning = GollekHealthProbe.isReachable(config.getGrpcHost(), config.getGrpcPort());
            if (isRunning) {
                return; // Already running
            }
            
            if (config.isAutoStart()) {
                if (config.isAutoInstall()) {
                    boolean installed = GollekInstaller.install(config);
                    if (!installed && config.isFailHard()) {
                        throw new RuntimeException("Failed to install Gollek backend. Cannot proceed.");
                    }
                }
                
                boolean started = GollekProcessManager.start(config);
                if (started) {
                    boolean healthy = GollekHealthProbe.waitForHealth(config.getGrpcHost(), config.getGrpcPort(), 30);
                    if (healthy) {
                        return; // Successfully started and healthy
                    } else {
                        System.err.println("[Wayang] Gollek backend started but did not become healthy within timeout.");
                    }
                }
                
                if (config.isFailHard()) {
                    throw new RuntimeException("Gollek backend is required but failed to start or become healthy.");
                }
            } else if (config.isFailHard()) {
                throw new RuntimeException("Gollek backend is not running at " + config.getGrpcHost() + ":" + config.getGrpcPort() + " and auto-start is disabled.");
            }
            
            // If failHard is false, we proceed (Wayang config might handle fallback)
            System.err.println("[Wayang] Warning: Gollek backend is not running. Falling back to " + config.getFallbackProvider());
            // Since this runs during CLI startup, we could inject a system property to override the preferred provider
            System.setProperty("wayang.fallback.provider", config.getFallbackProvider());
        }
    }
}
