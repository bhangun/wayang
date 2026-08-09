package tech.kayys.wayang.tool.nono;

import tech.kayys.wayang.spi.sandbox.Sandbox;
import tech.kayys.wayang.spi.sandbox.SandboxConfiguration;
import tech.kayys.wayang.spi.sandbox.SandboxProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

@ApplicationScoped
public class NonoSandboxProvider implements SandboxProvider {

    @Override
    public String getProviderId() {
        return "nono";
    }

    @Override
    public Sandbox createSandbox(SandboxConfiguration config) throws Exception {
        if (!NonoSandbox.isSupported()) {
            throw new IllegalStateException("Nono sandbox is not supported on this operating system or architecture.");
        }
        
        NonoSandbox sandbox = new NonoSandbox();
        
        // Apply working directory configuration
        if (config.getWorkingDirectory() != null) {
            Path workDir = Paths.get(config.getWorkingDirectory());
            if (!Files.exists(workDir)) {
                Files.createDirectories(workDir);
            }
            // Allow read/write access to the working directory and its children
            sandbox.allowPath(config.getWorkingDirectory(), NonoAccessMode.READ_WRITE);
        }

        return sandbox;
    }
}
