package tech.kayys.wayang.tool.nono;

import tech.kayys.wayang.spi.sandbox.Sandbox;
import tech.kayys.wayang.spi.sandbox.SandboxConfiguration;
import tech.kayys.wayang.spi.sandbox.SandboxProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Sandbox provider that creates Nono-backed {@link Sandbox} instances.
 *
 * <p>Registered as a Quarkus CDI bean.  The platform can inject this provider
 * via the {@code SandboxProvider} SPI and call {@link #createSandbox} to
 * obtain a fully configured, ready-to-apply sandbox.
 *
 * <p>Configuration is driven by the {@link SandboxConfiguration} passed at
 * creation time.  Extended Nono-specific options (network mode, blocked
 * commands, etc.) can be placed in the configuration's environment variables
 * with the following keys:
 * <ul>
 *   <li>{@code nono.network.mode} — "BLOCKED" | "ALLOW_ALL" | "PROXY_ONLY"</li>
 *   <li>{@code nono.network.proxy.port} — integer port for PROXY_ONLY mode</li>
 *   <li>{@code nono.commands.blocked} — comma-separated list of blocked commands</li>
 *   <li>{@code nono.commands.allowed} — comma-separated list of allowed commands</li>
 * </ul>
 */
@ApplicationScoped
public class NonoSandboxProvider implements SandboxProvider {

    @Override
    public String getProviderId() {
        return "nono";
    }

    @Override
    public tech.kayys.wayang.identity.ResourceId id() {
        return tech.kayys.wayang.identity.ResourceId.from(
            tech.kayys.wayang.extension.Id.fromString(java.util.UUID.nameUUIDFromBytes("nono-sandbox-provider".getBytes()).toString()),
            new tech.kayys.wayang.resource.ResourceType.Custom("sandbox-provider")
        );
    }

    @Override
    public tech.kayys.wayang.resource.ResourceType type() {
        return new tech.kayys.wayang.resource.ResourceType.Custom("sandbox-provider");
    }

    @Override
    public tech.kayys.wayang.extension.Metadata metadata() {
        return tech.kayys.wayang.extension.Metadata.builder()
            .name("NonoSandboxProvider")
            .version(tech.kayys.wayang.extension.Version.parse("1.0.0"))
            .description("Nono capability-based sandbox provider")
            .build();
    }

    @Override
    public Sandbox createSandbox(SandboxConfiguration config) throws Exception {
        if (!NonoSandbox.isSupported()) {
            throw new IllegalStateException(
                "Nono sandbox is not supported on this OS/architecture. " +
                "Platform: " + System.getProperty("os.name"));
        }

        NonoSandbox sandbox = new NonoSandbox();

        // ── Working directory ──────────────────────────────────────────────────
        if (config.getWorkingDirectory() != null) {
            var workDir = Paths.get(config.getWorkingDirectory());
            if (!Files.exists(workDir)) {
                Files.createDirectories(workDir);
            }
            sandbox.allowPath(config.getWorkingDirectory(), NonoAccessMode.READ_WRITE);
        }

        // ── Extended options via environment variable map ──────────────────────
        var env = config.getEnvironmentVariables();
        if (env != null) {
            // Network mode
            String netMode = env.getOrDefault("nono.network.mode", "BLOCKED");
            try {
                sandbox.setNetworkMode(NonoNetworkMode.valueOf(netMode.toUpperCase()));
            } catch (IllegalArgumentException e) {
                sandbox.setNetworkMode(NonoNetworkMode.BLOCKED);
            }

            // Proxy port
            String proxyPortStr = env.get("nono.network.proxy.port");
            if (proxyPortStr != null) {
                try {
                    sandbox.setProxyPort(Short.parseShort(proxyPortStr));
                } catch (NumberFormatException ignored) {}
            }

            // Blocked commands
            String blockedCmds = env.get("nono.commands.blocked");
            if (blockedCmds != null && !blockedCmds.isBlank()) {
                for (String cmd : blockedCmds.split(",")) {
                    String trimmed = cmd.trim();
                    if (!trimmed.isEmpty()) sandbox.blockCommand(trimmed);
                }
            }

            // Allowed commands
            String allowedCmds = env.get("nono.commands.allowed");
            if (allowedCmds != null && !allowedCmds.isBlank()) {
                for (String cmd : allowedCmds.split(",")) {
                    String trimmed = cmd.trim();
                    if (!trimmed.isEmpty()) sandbox.allowCommand(trimmed);
                }
            }
        }

        sandbox.deduplicate();
        return sandbox;
    }
}
