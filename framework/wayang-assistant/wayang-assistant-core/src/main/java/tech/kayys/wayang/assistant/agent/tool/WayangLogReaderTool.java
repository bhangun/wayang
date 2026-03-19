package tech.kayys.wayang.assistant.agent.tool;

import jakarta.enterprise.context.ApplicationScoped;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import tech.kayys.wayang.tool.spi.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tool to read the last N lines of server and UI logs.
 */
@ApplicationScoped
public class WayangLogReaderTool implements Tool {

    private static final Logger LOG = Logger.getLogger(WayangLogReaderTool.class);

    @ConfigProperty(name = "wayang.logs.server.path", defaultValue = "target/quarkus.log")
    String serverLogPath;

    @ConfigProperty(name = "wayang.logs.ui.path")
    String uiLogPath;

    @Override
    public String id() {
        return "wayang-log-reader";
    }

    @Override
    public String name() {
        return "Wayang Log Reader";
    }

    @Override
    public String description() {
        return "Read the last N lines of the Wayang server or UI/Desktop logs for debugging and troubleshooting.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "logType", Map.of(
                    "type", "string",
                    "enum", List.of("SERVER", "UI"),
                    "description", "The type of log to read (SERVER or UI)"
                ),
                "maxLines", Map.of(
                    "type", "integer",
                    "description", "Maximum number of lines to return (default 50)",
                    "default", 50
                )
            ),
            "required", List.of("logType")
        );
    }

    @Override
    public Uni<Map<String, Object>> execute(Map<String, Object> arguments, Map<String, Object> context) {
        String logType = (String) arguments.get("logType");
        int maxLines = (int) arguments.getOrDefault("maxLines", 50);

        String pathStr = "SERVER".equalsIgnoreCase(logType) ? serverLogPath : uiLogPath;
        if (pathStr == null || pathStr.isBlank()) {
            return Uni.createFrom().failure(new IllegalArgumentException("Log path not configured for: " + logType));
        }

        // Resolve ${user.home} manually if MicroProfile doesn't do it automatically in all environments
        pathStr = pathStr.replace("${user.home}", System.getProperty("user.home"));
        Path path = Paths.get(pathStr);

        if (!Files.exists(path)) {
            return Uni.createFrom().item(Map.of(
                "success", false,
                "error", "Log file not found: " + pathStr,
                "path", pathStr
            ));
        }

        try {
            List<String> lines = readLastLines(path, maxLines);
            return Uni.createFrom().item(Map.of(
                "success", true,
                "logType", logType,
                "path", pathStr,
                "lineCount", lines.size(),
                "content", String.join("\n", lines)
            ));
        } catch (IOException e) {
            LOG.errorf("Error reading log file: %s", e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }

    private List<String> readLastLines(Path path, int n) throws IOException {
        try (Stream<String> stream = Files.lines(path)) {
            List<String> allLines = stream.collect(Collectors.toList());
            int size = allLines.size();
            int start = Math.max(0, size - n);
            return allLines.subList(start, size);
        }
    }
}
