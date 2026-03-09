package tech.kayys.wayang.agent.executor.audit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.core.audit.AgentArtifact;
import tech.kayys.wayang.agent.core.audit.AgentAuditService;

/**
 * Stores agent artifacts as files (e.g., Markdown) on the local filesystem.
 */
@ApplicationScoped
public class FileAgentAuditService implements AgentAuditService {

    private static final Logger LOG = LoggerFactory.getLogger(FileAgentAuditService.class);

    @ConfigProperty(name = "wayang.agent.audit.file.directory", defaultValue = "/tmp/wayang/audit")
    String auditDirectory;

    @Override
    public Uni<Void> saveArtifact(AgentArtifact artifact) {
        try {
            Path runDir = Paths.get(auditDirectory, artifact.tenantId(), artifact.runId());
            if (!Files.exists(runDir)) {
                Files.createDirectories(runDir);
            }

            String ext = "markdown".equals(artifact.format()) ? ".md" : "." + artifact.format();
            String filename = String.format("%s_%s%s", artifact.type(), artifact.id(), ext);
            Path filePath = runDir.resolve(filename);

            String header = String.format("---%nID: %s%nRunID: %s%nType: %s%nDate: %s%n---%n%n",
                    artifact.id(), artifact.runId(), artifact.type(), artifact.createdAt());
            
            String contentToWrite = "markdown".equals(artifact.format()) 
                    ? header + artifact.content() 
                    : artifact.content();

            Files.writeString(filePath, contentToWrite);
            LOG.debug("Saved audit artifact to {}", filePath);
            return Uni.createFrom().voidItem();
        } catch (IOException e) {
            LOG.error("Failed to write audit artifact to file", e);
            return Uni.createFrom().failure(e);
        }
    }

    @Override
    public Uni<AgentArtifact> getArtifact(String artifactId, String tenantId) {
        // Filename format cannot easily be inferred without runId via filesystem alone
        // without scanning. Returning null/empty for now as the filesystem impl
        // is mostly write-centric for debugging.
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<List<AgentArtifact>> getArtifactsByRun(String runId, String tenantId) {
        Path runDir = Paths.get(auditDirectory, tenantId, runId);
        if (!Files.exists(runDir)) {
            return Uni.createFrom().item(new ArrayList<>());
        }

        try {
            List<AgentArtifact> artifacts = new ArrayList<>();
            try (var stream = Files.walk(runDir, 1)) {
                stream.filter(path -> Files.isRegularFile(path))
                      .forEach(path -> {
                          // Dummy artifact added just to represent the count properly for now
                          artifacts.add(new AgentArtifact(path.getFileName().toString(), runId, tenantId, "file", "", "markdown", java.util.Map.of(), java.time.Instant.now()));
                          LOG.debug("Found artifact file: {}", path);
                      });
            }
            return Uni.createFrom().item(artifacts);
        } catch (IOException e) {
            LOG.error("Failed to read audit directory", e);
            return Uni.createFrom().failure(e);
        }
    }
}
