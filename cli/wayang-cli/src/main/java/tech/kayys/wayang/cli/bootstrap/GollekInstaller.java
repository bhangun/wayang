package tech.kayys.wayang.cli.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class GollekInstaller {

    public static boolean install(GollekConfig config) {
        Path installDir = Paths.get(config.getInstallDir());
        Path binaryPath = installDir.resolve("bin").resolve("gollek");
        
        if (Files.exists(binaryPath)) {
            return true;
        }
        
        System.out.println("[Wayang] Gollek binary not found. Initiating download and install...");
        
        try {
            Files.createDirectories(installDir);
            
            String scriptSource = config.getDownloadScript();
            
            if (scriptSource != null && (scriptSource.startsWith("http://") || scriptSource.startsWith("https://"))) {
                System.out.println("[Wayang] Fetching and running install script from " + scriptSource + " ...");
                // Use curl to pipe the script to bash
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", "curl -sL " + scriptSource + " | bash -s -- --target " + installDir.toString());
                pb.inheritIO();
                Process process = pb.start();
                
                boolean finished = process.waitFor(5, TimeUnit.MINUTES);
                if (finished && process.exitValue() == 0) {
                    System.out.println("[Wayang] Gollek installed successfully via remote script.");
                    return true;
                } else {
                    System.err.println("[Wayang] Failed to install Gollek via remote script. Falling back to direct binary download.");
                }
            }
            
            // Direct download fallback
            System.out.println("[Wayang] Attempting direct binary download from " + config.getDownloadUrl() + "...");
            Files.createDirectories(installDir.resolve("bin"));
            
            ProcessBuilder pb = new ProcessBuilder("curl", "-sL", "-o", binaryPath.toString(), config.getDownloadUrl());
            pb.inheritIO();
            Process process = pb.start();
            if (process.waitFor() == 0) {
                binaryPath.toFile().setExecutable(true);
                System.out.println("[Wayang] Gollek downloaded directly.");
                return true;
            } else {
                System.err.println("[Wayang] Direct download failed.");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("[Wayang] Error during Gollek installation: " + e.getMessage());
            return false;
        }
    }
}
