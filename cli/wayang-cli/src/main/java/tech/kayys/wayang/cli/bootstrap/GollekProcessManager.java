package tech.kayys.wayang.cli.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GollekProcessManager {
    
    private static Process gollekProcess;

    public static boolean start(GollekConfig config) {
        Path installDir = Paths.get(config.getInstallDir());
        Path binaryPath = installDir.resolve("bin").resolve("gollek");
        
        if (!Files.exists(binaryPath)) {
            System.err.println("[Wayang] Cannot start Gollek. Binary not found at " + binaryPath);
            return false;
        }

        try {
            System.out.println("[Wayang] Starting Gollek backend on port " + config.getGrpcPort() + "...");
            ProcessBuilder pb = new ProcessBuilder(
                binaryPath.toString(),
                "serve",
                "--grpc-port", String.valueOf(config.getGrpcPort())
            );
            
            // Redirect output to logs
            Path logDir = Paths.get(System.getProperty("user.home"), ".wayang", "logs");
            Files.createDirectories(logDir);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logDir.resolve("gollek-backend.log").toFile()));
            
            gollekProcess = pb.start();
            
            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (gollekProcess != null && gollekProcess.isAlive()) {
                    System.out.println("[Wayang] Stopping Gollek backend...");
                    gollekProcess.destroy();
                }
            }));
            
            return true;
        } catch (IOException e) {
            System.err.println("[Wayang] Failed to start Gollek process: " + e.getMessage());
            return false;
        }
    }
    
    public static void stop() {
        if (gollekProcess != null && gollekProcess.isAlive()) {
            gollekProcess.destroy();
        }
    }
}
