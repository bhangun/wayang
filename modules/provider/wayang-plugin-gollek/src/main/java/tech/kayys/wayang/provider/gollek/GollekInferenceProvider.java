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

    private static final Object PROCESS_LOCK = new Object();
    private static Process activeProcess = null;

    // ── stream ────────────────────────────────────────────────────────────────

    @Override
    
    @Override
    public CompletionStream stream(CompletionRequest request) throws Exception {
        String grpcTarget = System.getenv("GOLLEK_GRPC_TARGET");
        if (grpcTarget == null || grpcTarget.isBlank()) {
            grpcTarget = "localhost:9131"; // Use gRPC by default!
        }
        
        try {
            return streamGrpc(request, grpcTarget);
        } catch (Exception e) {
            System.err.println("[Gollek] gRPC failed, falling back to CLI. Error: " + e.getMessage());
            return streamCli(request);
        }
    }

    private CompletionStream streamGrpc(CompletionRequest request, String target) {
        String model = request.model() != null && !request.model().isBlank()
                ? request.model() : "default";
        int maxTokens = request.maxTokens() > 0 ? request.maxTokens() : 2048;

        tech.kayys.gollek.protobuf.ChatRequest.Builder reqBuilder = tech.kayys.gollek.protobuf.ChatRequest.newBuilder()
                .setModelId("default")
                .setMaxTokens(maxTokens)
                .setTemperature((float) (request.temperature() != null ? request.temperature() : 0.7));

        if (request.messages() != null) {
            for (Message m : request.messages()) {
                String roleStr = m.role() != null ? m.role().name().toLowerCase() : "user";
                if (roleStr.equals("system") && (m.content() == null || m.content().isBlank())) {
                    continue;
                }
                reqBuilder.addMessages(tech.kayys.gollek.protobuf.Message.newBuilder().setRole(roleStr).setContent(m.content() != null ? m.content() : "").build());
            }
        }

        io.grpc.ManagedChannel channel = io.grpc.ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        tech.kayys.gollek.protobuf.GollekServiceGrpc.GollekServiceBlockingStub stub = tech.kayys.gollek.protobuf.GollekServiceGrpc.newBlockingStub(channel);
        
        // This will block until the connection is established or fails
        java.util.Iterator<tech.kayys.gollek.protobuf.ChatResponse> iter = stub.streamChat(reqBuilder.build());
        final String streamId = "g-" + UUID.randomUUID();

        return new CompletionStream() {
            private tech.kayys.gollek.protobuf.ChatResponse nextVal = null;
            private boolean done = false;

            private void fetch() {
                if (nextVal != null || done) return;
                try {
                    if (iter.hasNext()) {
                        nextVal = iter.next();
                    } else {
                        done = true;
                    }
                } catch (Exception e) {
                    System.err.println("[Gollek] gRPC stream failed: " + e.getMessage());
                    e.printStackTrace();
                    done = true;
                }
            }

            @Override public boolean hasNext() {
                fetch();
                return !done;
            }

            @Override public CompletionResult next() {
                fetch();
                if (done || nextVal == null) return CompletionResult.of("");
                String content = nextVal.getMessage() != null ? nextVal.getMessage().getContent() : "";
                nextVal = null;
                return CompletionResult.of(content);
            }

            @Override public void close() {
                done = true;
                if (channel != null) {
                    channel.shutdownNow();
                }
            }

            @Override public boolean isComplete() { return done; }
            @Override public String getStreamId() { return streamId; }
        };
    }

    private CompletionStream streamCli(CompletionRequest request) throws Exception {

        String model  = request.model() != null && !request.model().isBlank()
                        ? request.model() : "hf:unsloth/gemma-4-12b-it-gguf";
        String prompt = buildPrompt(request.messages(), model);

        int maxTokens = request.maxTokens() > 0 ? request.maxTokens() : 512;

        ProcessBuilder pb = new ProcessBuilder(GOLLEK_CLI, "run",
                "--model", model,
                "--max-tokens", String.valueOf(maxTokens),
                "--prompt", prompt);
        pb.environment().put("GGUF_CONTEXT_SIZE", "4096");
        pb.redirectErrorStream(true);

        Process process;
        synchronized (PROCESS_LOCK) {
            if (activeProcess != null) {
                try {
                    if (activeProcess.isAlive()) {
                        activeProcess.destroyForcibly();
                        activeProcess.waitFor(1000, TimeUnit.MILLISECONDS);
                    }
                    // Add a delay to allow macOS/Metal to reclaim the 7GB+ of unified memory 
                    // before we spawn the next process, preventing memory pressure spikes.
                    Thread.sleep(1500); 
                } catch (Exception ignored) {}
            }
            process = pb.start();
            activeProcess = process;
        }

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
            private boolean preamblePassed = false;
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

            private boolean isPreamble(String line) {
                if (line == null) return true;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) return true;
                if (trimmed.startsWith("Resolved") ||
                    trimmed.startsWith("Model:") ||
                    trimmed.startsWith("Provider:") ||
                    trimmed.startsWith("Execution route:") ||
                    trimmed.startsWith("Using ") ||
                    trimmed.startsWith("-") ||
                    trimmed.startsWith("=") ||
                    trimmed.startsWith("_") ||
                    trimmed.startsWith("/") ||
                    trimmed.startsWith("|") ||
                    trimmed.startsWith("\\") ||
                    trimmed.contains("____") ||
                    trimmed.contains("|_|")) {
                    return true;
                }
                return false;
            }

            /**
             * Process a single line from the subprocess.
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
                        emitMetricsIfAny();
                        inMetrics = false;
                    }
                }

                if (!preamblePassed) {
                    if (isPreamble(line)) {
                        return; // Discard CLI banner / loader info lines
                    }
                    preamblePassed = true;
                }

                // If channel marker present:
                if (line.startsWith(CHANNEL_OPEN)) {
                    state = State.CHAN_HEADER;
                    return;
                }

                if (state == State.SKIP) {
                    // Standard text output (no channel wrapper)
                    state = State.RESPONSE;
                }

                switch (state) {
                    case CHAN_HEADER, THINKING -> emitContentLine(line);
                    case RESPONSE -> {
                        emitText(line + "\n");
                    }
                    case DONE -> { /* no-op */ }
                    default -> emitText(line + "\n");
                }
            }
            
            private void emitMetricsIfAny() {
                try {
                    if (this.metricsBuf != null && this.metricsBuf.length() > 0) {
                        this.metricsBuf.setLength(0);
                    }
                } catch (NullPointerException e) {
                    // Ignore if metricsBuf is not initialized yet during fillBuf()
                }
            }

            private void emitContentLine(String line) {
                int sep = line.indexOf(CHANNEL_START);
                if (sep >= 0) {
                    String thinking = line.substring(0, sep);
                    if (!thinking.isEmpty()) {
                        emitText(thinking + "\n");
                    }
                    String response = line.substring(sep + CHANNEL_START.length());
                    state = State.RESPONSE;
                    if (!response.isEmpty()) {
                        emitText(response + "\n");
                    }
                } else {
                    state = State.THINKING;
                    if (!line.isEmpty()) {
                        emitText(line + "\n");
                    }
                }
            }

            private void emitText(String text) {
                if (text != null && !text.isEmpty()) {
                    buf.add(text);
                }
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
                synchronized (PROCESS_LOCK) {
                    if (activeProcess == process) {
                        activeProcess = null;
                    }
                }
                if (process.isAlive()) {
                    process.destroyForcibly();
                    try { process.waitFor(1000, TimeUnit.MILLISECONDS); } catch (Exception ignored) {}
                }
                // Allow macOS Metal some time to reclaim the massive memory footprint
                try { Thread.sleep(1500); } catch (Exception ignored) {}
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
