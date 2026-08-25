package tech.kayys.wayang.provider.gollek;

import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.inference.InferenceProvider;
import tech.kayys.wayang.inference.CompletionRequest;
import tech.kayys.wayang.inference.CompletionResult;
import tech.kayys.wayang.inference.CompletionStream;
import tech.kayys.wayang.inference.ModelInfo;
import tech.kayys.wayang.inference.Message;
import tech.kayys.wayang.inference.Choice;
import tech.kayys.wayang.provider.ModelsDevRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Gollek InferenceProvider — delegates to the gollek CLI subprocess.
 *
 * <h2>Channel protocol (gollek gemma4 / GGUF runner)</h2>
 * <pre>
 *   &lt;|channel&gt;thought\n
 *   [optional scratch / internal reasoning text]
 *   &lt;channel|&gt;FINAL ANSWER TOKENS…
 *   \n[Fast GGUF, Duration: …]
 *   Performance Metrics:
 *   …
 * </pre>
 *
 * <ul>
 *   <li>Text between the channel-open line and {@code &lt;channel|&gt;} is
 *       <b>internal reasoning / scratch</b> — emitted with type {@code "thinking"}.</li>
 *   <li>Text <em>after</em> {@code &lt;channel|&gt;} is the <b>final answer</b> — emitted
 *       with type {@code "response"}.</li>
 *   <li>Everything before the first {@code &lt;|channel&gt;} line (banner, info) is discarded.</li>
 *   <li>Performance metrics are discarded.</li>
 * </ul>
 *
 * <p>Each token is wrapped as a compact JSON object so the Flutter UI can route it to the
 * correct bubble widget:
 * <pre>{"t":"response","d":"H"}</pre>
 */
public class GollekInferenceProvider implements InferenceProvider {
    public static final String ID      = "gollek";
    public static final String VERSION = "1.0.0";

    private static final String CHANNEL_OPEN  = "<|channel>";   // marks start of channel header
    private static final String CHANNEL_START = "<channel|>";   // marks start of content in channel
    private static final String STOP_MARKER_1 = "[Fast GGUF";
    private static final String STOP_MARKER_2 = "Performance Metrics";

    // ── CLI binary resolution ────────────────────────────────────────────────

    private static final String GOLLEK_CLI;
    static {
        String envCli = System.getenv("GOLLEK_CLI");
        if (envCli != null && Files.isExecutable(Path.of(envCli))) {
            GOLLEK_CLI = envCli;
        } else if (Files.isExecutable(Path.of(System.getProperty("user.home") + "/.local/bin/gollek"))) {
            GOLLEK_CLI = System.getProperty("user.home") + "/.local/bin/gollek";
        } else if (Files.isExecutable(Path.of("/usr/local/bin/gollek"))) {
            GOLLEK_CLI = "/usr/local/bin/gollek";
        } else {
            GOLLEK_CLI = "gollek";
        }
    }

    // ── Identity ─────────────────────────────────────────────────────────────

    private final ResourceId stableId = ResourceId.from(
            Id.fromUUID(UUID.nameUUIDFromBytes(ID.getBytes(java.nio.charset.StandardCharsets.UTF_8))),
            new ResourceType.Plugin());

    @Override public ResourceId id()       { return stableId; }
    @Override public String    getId()     { return ID; }
    @Override public ResourceType type()   { return new ResourceType.Plugin(); }
    @Override public void initialize()     {}

    @Override
    public Metadata metadata() {
        return Metadata.builder()
                .name("Gollek Local Engine")
                .description("Local LLM engine powered by Gollek (FFM API / Metal)")
                .version(VERSION)
                .build();
    }

    // ── generate (blocking) ───────────────────────────────────────────────────

    @Override
    public CompletionResult generate(CompletionRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        CompletionStream cs = stream(request);
        try {
            while (cs.hasNext()) {
                CompletionResult r = cs.next();
                if (r.getContent() != null) sb.append(r.getContent());
            }
        } finally {
            cs.close();
        }
        return CompletionResult.of(sb.toString());
    }

    // ── stream ────────────────────────────────────────────────────────────────

    @Override
    public CompletionStream stream(CompletionRequest request) throws Exception {
        String model  = request.model() != null && !request.model().isBlank()
                        ? request.model() : "hf:unsloth/gemma-4-12b-it-gguf";
        String prompt = buildPrompt(request.messages(), model);

        int maxTokens = request.maxTokens() > 0 ? request.maxTokens() : 1024;

        ProcessBuilder pb = new ProcessBuilder(GOLLEK_CLI, "run",
                "--model", model,
                "--max-tokens", String.valueOf(maxTokens),
                "--prompt", prompt);
        pb.environment().put("GGUF_CONTEXT_SIZE", "8192");
        pb.redirectErrorStream(true);

        Process        process  = pb.start();
        BufferedReader reader   = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final String   streamId = "g-" + UUID.randomUUID();

        return new CompletionStream() {
// ... unchanged stream implementation ...
            private enum State { SKIP, CHAN_HEADER, THINKING, RESPONSE, DONE }

            private State         state       = State.SKIP;
            private final StringBuilder lineAccum = new StringBuilder(); // accumulates one line
            private final ArrayDeque<String> buf = new ArrayDeque<>();   // ready-to-emit tokens
            private boolean       done        = false;
            private boolean inMetrics = false;
            private final StringBuilder metricsBuf = new StringBuilder();

            // Pre-fill on construction
            { fillBuf(); }

            // ── internal helpers ─────────────────────────────────────────────

            /** Read lines from the subprocess until we have tokens in {@code buf} or EOF. */
            private void fillBuf() {
                while (buf.isEmpty() && !done) {
                    String line;
                    try {
                        line = reader.readLine();
                    } catch (IOException e) {
                        done = true;
                        return;
                    }
                    if (line == null) { done = true; return; }
                    processLine(line);
                }
            }

            /**
             * Process a single line from the subprocess.
             *
             * <p>A line may contain an embedded {@code <channel|>} separator, so we
             * split on it when we are in the THINKING state.
             */
            private void processLine(String line) {
                // ── stop on metrics markers ──────────────────────────────────
                if (line.startsWith(STOP_MARKER_1) || line.startsWith(STOP_MARKER_2)) {
                    emitMetricsIfAny();
                    done = true;
                    return;
                }
                
                if (line.startsWith("[Fast GGUF") || line.startsWith("Performance Metrics:")) {
                    inMetrics = true;
                    metricsBuf.append(line).append("\n");
                    return;
                }
                if (inMetrics) {
                    if (line.isBlank() || line.startsWith("  ")) {
                        metricsBuf.append(line).append("\n");
                        return;
                    } else {
                        // Metrics block ended
                        emitMetricsIfAny();
                        inMetrics = false;
                    }
                }

                switch (state) {
                    case SKIP -> {
                        if (line.startsWith(CHANNEL_OPEN)) {
                            // e.g. "<|channel>thought"
                            state = State.CHAN_HEADER;
                            // channel name is the rest of this line (after <|channel>)
                            // We don't need the name for now; all content goes to
                            // thinking/response based on position relative to <channel|>
                        }
                        // anything else in SKIP → discard (banner, info lines)
                    }
                    case CHAN_HEADER -> {
                        // This is the line AFTER <|channel>NAME; it may start with
                        // <channel|> immediately (no scratch) or contain scratch text.
                        emitContentLine(line);
                    }
                    case THINKING -> {
                        emitContentLine(line);
                    }
                    case RESPONSE -> {
                        // We are in the response section; emit the whole line as response.
                        emitText("response", line);
                        emitText("response", "\n");
                    }
                    case DONE -> { /* no-op */ }
                }
            }
            
            private void emitMetricsIfAny() {
                if (metricsBuf != null && metricsBuf.length() > 0) {
                    String data = metricsBuf.toString().trim();
                    buf.add("{\"t\":\"metrics\",\"d\":\"" + jsonEscape(data) + "\"}");
                    metricsBuf.setLength(0);
                }
            }
            
            private String jsonEscape(String value) {
                if (value == null) return "";
                return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
            }

            /**
             * Handle a line that may straddle the thinking/response boundary.
             * If the line contains {@code <channel|>}, the text before it is
             * thinking (scratch) and the text after is response.
             */
            private void emitContentLine(String line) {
                int sep = line.indexOf(CHANNEL_START);
                if (sep >= 0) {
                    // Part before <channel|> is scratch/thinking
                    String thinking = line.substring(0, sep);
                    if (!thinking.isEmpty()) {
                        emitText("thinking", thinking);
                        emitText("thinking", "\n");
                    }
                    // Part after <channel|> is the response
                    String response = line.substring(sep + CHANNEL_START.length());
                    state = State.RESPONSE;
                    if (!response.isEmpty()) {
                        emitText("response", response);
                        emitText("response", "\n");
                    }
                } else {
                    // Whole line is thinking/scratch
                    state = State.THINKING;
                    if (!line.isEmpty()) {
                        emitText("thinking", line);
                        emitText("thinking", "\n");
                    }
                }
            }

            /** Enqueue individual character tokens for a given channel type. */
            private void emitText(String channel, String text) {
                for (int i = 0; i < text.length(); i++) {
                    buf.add(jsonToken(channel, text.charAt(i)));
                }
            }

            /** Produce a compact JSON token: {@code {"t":"channel","d":"char"}} */
            private String jsonToken(String channel, char c) {
                String d = switch (c) {
                    case '\n' -> "\\n";
                    case '\r' -> "\\r";
                    case '\t' -> "\\t";
                    case '"'  -> "\\\"";
                    case '\\' -> "\\\\";
                    default   -> String.valueOf(c);
                };
                return "{\"t\":\"" + channel + "\",\"d\":\"" + d + "\"}";
            }

            // ── CompletionStream interface ────────────────────────────────────

            @Override public boolean hasNext() {
                if (!buf.isEmpty()) return true;
                fillBuf();
                if (buf.isEmpty() && done) {
                    emitMetricsIfAny();
                }
                return !buf.isEmpty();
            }

            @Override public CompletionResult next() {
                if (buf.isEmpty()) fillBuf();
                return CompletionResult.of(buf.isEmpty() ? "" : buf.poll());
            }

            @Override public void close() {
                done = true;
                process.destroyForcibly();
                try { reader.close(); } catch (IOException ignored) {}
            }

            @Override public boolean isComplete()  { return done && buf.isEmpty(); }
            @Override public String  getStreamId() { return streamId; }
        };
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static String buildPrompt(List<Message> messages, String model) {
        if (messages == null || messages.isEmpty()) return "";
        if (messages.size() == 1) return messages.get(0).content();

        java.util.List<tech.kayys.gollek.spi.Message> mapped = new java.util.ArrayList<>();
        for (Message m : messages) {
            tech.kayys.gollek.spi.Message.Role r;
            if (m.role() != null) {
                switch (m.role().name()) {
                    case "SYSTEM": r = tech.kayys.gollek.spi.Message.Role.SYSTEM; break;
                    case "ASSISTANT": r = tech.kayys.gollek.spi.Message.Role.ASSISTANT; break;
                    case "TOOL": r = tech.kayys.gollek.spi.Message.Role.TOOL; break;
                    default: r = tech.kayys.gollek.spi.Message.Role.USER; break;
                }
            } else {
                r = tech.kayys.gollek.spi.Message.Role.USER;
            }
            mapped.add(new tech.kayys.gollek.spi.Message(r, m.content()));
        }

        return tech.kayys.gollek.models.core.ChatTemplateFormatter.format(mapped, model);
    }

    private static final long MODEL_CACHE_TTL_MS = 10_000L;
    private long lastModelFetchTime = 0;
    private final Map<String, ModelInfo> cachedModelInfos = new LinkedHashMap<>();

    private synchronized void refreshGollekModels() {
        long now = System.currentTimeMillis();
        if (now - lastModelFetchTime < MODEL_CACHE_TTL_MS && !cachedModelInfos.isEmpty()) {
            return;
        }
        cachedModelInfos.clear();
        try {
            ProcessBuilder pb = new ProcessBuilder(GOLLEK_CLI, "list", "-f=json");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            if (finished && p.exitValue() == 0) {
                String json = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
                if (root.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode node : root) {
                        String id = node.hasNonNull("id") ? node.get("id").asText() : "";
                        String name = node.hasNonNull("name") ? node.get("name").asText() : id;
                        String format = node.hasNonNull("format") ? node.get("format").asText() : "gguf";
                        String taskType = node.hasNonNull("taskType") ? node.get("taskType").asText() : "text";
                        long size = node.hasNonNull("size") ? node.get("size").asLong() : 0L;

                        if (!id.isBlank()) {
                            ModelInfo info = new ModelInfo(
                                    id,
                                    name,
                                    format,
                                    Set.of("chat", "streaming", taskType),
                                    Map.of("sizeBytes", size, "format", format, "taskType", taskType)
                            );
                            cachedModelInfos.put(id, info);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        if (cachedModelInfos.isEmpty()) {
            cachedModelInfos.put("hf:unsloth/gemma-4-12b-it-GGUF", new ModelInfo("hf:unsloth/gemma-4-12b-it-GGUF", "gemma-4-12b-it-GGUF", "gguf", Set.of("chat"), Map.of()));
            cachedModelInfos.put("gemma-2-2b-it-Q4_K_M", new ModelInfo("gemma-2-2b-it-Q4_K_M", "gemma-2-2b-it-Q4_K_M", "gguf", Set.of("chat"), Map.of()));
        }
        lastModelFetchTime = now;
    }

    @Override
    public Set<String> listModels() {
        refreshGollekModels();
        return Collections.unmodifiableSet(cachedModelInfos.keySet());
    }

    @Override
    public ModelInfo getModelInfo(String modelId) {
        refreshGollekModels();
        ModelInfo info = cachedModelInfos.get(modelId);
        if (info != null) return info;
        return new ModelInfo(modelId, modelId, "gguf", Set.of("chat", "streaming"), Map.of());
    }
}
