package tech.kayys.wayang.gollek.cli.code;

import tech.kayys.peutui.agent.AgentMessage;
import tech.kayys.peutui.core.autocomplete.AutocompleteItem;
import tech.kayys.peutui.core.autocomplete.AutocompleteProvider;
import tech.kayys.peutui.core.autocomplete.AutocompleteSuggestions;
import tech.kayys.peutui.core.autocomplete.CompletionEdit;
import tech.kayys.peutui.core.autocomplete.CompositeAutocompleteEngine;
import tech.kayys.peutui.terminal.JLineTerminalDriver;
import tech.kayys.peutui.terminal.TerminalDriver;
import tech.kayys.peutui.widgets.App;
import tech.kayys.peutui.widgets.ChatHistoryComponent;
import tech.kayys.peutui.widgets.HeaderComponent;
import tech.kayys.peutui.widgets.SpinnerComponent;
import tech.kayys.peutui.widgets.StatusBarComponent;
import tech.kayys.peutui.widgets.TextInputComponent;
import tech.kayys.peutui.widgets.VerticalStackComponent;

import tech.kayys.wayang.sdk.agent.PermissionDecision;
import tech.kayys.wayang.sdk.agent.WayangAgent;
import tech.kayys.wayang.sdk.agent.WayangAgentListener;
import tech.kayys.wayang.sdk.json.JsonValue;
import tech.kayys.wayang.tools.spi.ToolResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A standalone Peutui-based interactive REPL for the Wayang Agent.
 *
 * <p>Bridges the existing {@link WayangAgent} (callback-based
 * {@link WayangAgentListener}) to the Peutui widget stack — a
 * {@code ChatHistoryComponent} + {@code SpinnerComponent} +
 * {@code StatusBarComponent} + {@code TextInputComponent} — with no
 * dependency on CDI or QuarkusMain.</p>
 *
 * <p>Slash commands are passed to an external handler; a built-in
 * {@link CompositeAutocompleteEngine} provides Tab-completion for the
 * most common commands.</p>
 */
public final class WayangPeutuiApp {

    private static final ScheduledExecutorService SPINNER_TICKER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "wayang-peutui-spinner");
                t.setDaemon(true);
                return t;
            });

    private final WayangAgent agent;
    private final String agentId;
    private final String sessionId;
    private final String model;
    private final String provider;

    private tech.kayys.peutui.widgets.App appInstance;
    private tech.kayys.peutui.widgets.ModalOverlayComponent overlay;

    /**
     * Called for every slash command the user submits.
     * The consumer is used to output text to the chat history.
     * Return {@code true} to close the REPL.
     */
    private BiFunction<String, Consumer<String>, Boolean> slashHandler = (cmd, out) -> false;

    public WayangPeutuiApp(WayangAgent agent,
                            String agentId,
                            String sessionId,
                            String model,
                            String provider) {
        this.agent     = agent;
        this.agentId   = agentId   != null ? agentId   : "wayang";
        this.sessionId = sessionId != null ? sessionId : UUID.randomUUID().toString().substring(0, 8);
        this.model     = model     != null ? model     : "unknown";
        this.provider  = provider  != null ? provider  : "local";
    }

    /** Install an external slash-command handler. */
    public void setSlashHandler(BiFunction<String, Consumer<String>, Boolean> handler) {
        this.slashHandler = handler != null ? handler : (cmd, out) -> false;
    }

    public void showModal(tech.kayys.peutui.core.component.Component modal) {
        if (overlay != null && appInstance != null) {
            overlay.setModal(modal);
            appInstance.requestRender();
        }
    }

    public void hideModal() {
        if (overlay != null && appInstance != null) {
            overlay.hideModal();
            appInstance.requestRender();
        }
    }

    public void stop() {
        if (appInstance != null) {
            appInstance.stop();
        }
    }

    /**
     * Runs the interactive REPL and blocks until the user exits
     * (Ctrl+C or a slash handler that returns {@code true}).
     */
    public void run() throws InterruptedException {
        TerminalDriver driver = new JLineTerminalDriver();

        // ── Widget tree ────────────────────────────────────────────────────────
        ChatHistoryComponent history = new ChatHistoryComponent();
        history.setShowAgentTags(false);

        SpinnerComponent spinner = new SpinnerComponent();
        StatusBarComponent statusBar = new StatusBarComponent();

        TextInputComponent input = new TextInputComponent();
        input.setPromptPrefix("[" + agentId + "] › ");
        input.setPlaceholder("Message… · Enter=send · Tab=/commands · Ctrl+C=quit");

        HeaderComponent header = new HeaderComponent()
                .setTitle("Wayang Agent")
                .setSubtitle(provider + " / " + model)
                .addTab("Chat", "chat")
                .addTab("Models", "models")
                .addTab("Providers", "providers")
                .addTab("Projects", "projects")
                .addTab("Sessions", "sessions");

        VerticalStackComponent stack = new VerticalStackComponent()
                .addFixed(header, 3)
                .addFlexible(history)
                .addFixed(spinner,   1)
                .addFixed(statusBar, 1)
                .addFixed(input,     3);

        this.overlay = new tech.kayys.peutui.widgets.ModalOverlayComponent(stack);
        
        App app = new App(driver, overlay);
        this.appInstance = app;

        header.setOnTabSelected(index -> {
            if (index == 0) {
                stack.setFocusTarget(input);
                app.requestRender();
                return;
            }
            
            String cmd = switch (index) {
                case 1 -> "/models";
                case 2 -> "/providers";
                case 3 -> "/projects";
                case 4 -> "/sessions";
                default -> null;
            };
            
            if (cmd != null) {
                try {
                    boolean exit = slashHandler.apply(cmd, textOut -> {
                        if (textOut != null && !textOut.isEmpty()) {
                            history.append(AgentMessage.assistant("system", textOut));
                        }
                    });
                    if (exit) {
                        app.stop();
                    }
                } catch (Exception e) {
                    history.append(AgentMessage.assistant("system", "\u001B[31mError: " + e.getMessage() + "\u001B[0m"));
                }
            }
            
            // Switch back to chat tab and focus input
            header.setActiveTab(0);
            stack.setFocusTarget(input);
            app.requestRender();
        });

        app.setFocused(overlay);
        stack.setFocusTarget(input);

        // ── Status bar ─────────────────────────────────────────────────────────
        statusBar.setLeft("agent",   "✦ " + agentId);
        statusBar.setLeft("model",   "model:" + model);
        statusBar.setLeft("session", "session:" + sessionId.substring(0, Math.min(8, sessionId.length())));
        statusBar.setRight("hint",   "Ctrl+C to quit");

        // ── Autocomplete ───────────────────────────────────────────────────────
        input.setAutocomplete(buildSlashAutocomplete());

        // ── Welcome message ────────────────────────────────────────────────────
        history.append(AgentMessage.assistant("system",
                "Welcome! I am Wayang, your general-purpose AI assistant.\n" +
                "Currently using model \u001B[1m" + model + "\u001B[0m" +
                " via \u001B[1m" + provider + "\u001B[0m.\n" +
                "Type \u001B[36m/help\u001B[0m for available commands."));

        // ── Spinner bookkeeping ────────────────────────────────────────────────
        AtomicBoolean spinnerRunning = new AtomicBoolean(false);
        ScheduledFuture<?>[] tickHandle = {null};

        // ── Submit handler ─────────────────────────────────────────────────────
        input.setOnSubmit(text -> {
            if (text == null || text.isBlank()) return;

            // ── Slash commands ─────────────────────────────────────────────────
            if (text.trim().startsWith("/")) {
                String cmd = text.trim();

                // Built-in: local clear (no network call needed)
                if ("/clear".equals(cmd)) {
                    history.append(AgentMessage.assistant("system",
                            "\u001B[2m(History display cleared)\u001B[0m"));
                    app.requestRender();
                    return;
                }

                // Delegate to the external handler (returns true → exit)
                try {
                    boolean exit = slashHandler.apply(cmd, textOut -> {
                        if (textOut != null && !textOut.isEmpty()) {
                            history.append(AgentMessage.assistant("system", textOut));
                            app.requestRender();
                        }
                    });
                    if (exit) {
                        app.stop();
                    }
                } catch (Exception e) {
                    history.append(AgentMessage.assistant("system",
                            "\u001B[31mError: " + e.getMessage() + "\u001B[0m"));
                    app.requestRender();
                }
                return;
            }

            // ── Normal user message ────────────────────────────────────────────
            history.append(AgentMessage.user(text));
            app.requestRender();

            startSpinner(spinner, spinnerRunning, tickHandle, statusBar, app);

            // Placeholder while streaming
            StringBuilder streamed = new StringBuilder();
            history.append(AgentMessage.assistant(agentId, "\u001B[2m▊\u001B[0m"));

            // Run agent on a virtual thread to keep the render loop unblocked
            Thread.ofVirtual().name("wayang-agent-turn").start(() -> {
                try {
                    agent.send(text, new WayangAgentListener() {

                        @Override
                        public void onTextDelta(String delta) {
                            streamed.append(delta);
                            history.replaceLast(
                                    AgentMessage.assistant(agentId, streamed.toString()));
                            app.requestRender();
                        }

                        @Override
                        public void onThinkingDelta(String chunk) {
                            statusBar.setRight("thinking", "\u001B[2m✦ thinking…\u001B[0m");
                            app.requestRender();
                        }

                        @Override
                        public void onThinkingEnd() {
                            statusBar.remove("thinking");
                            app.requestRender();
                        }

                        @Override
                        public void onToolCallStart(String id, String name) {
                            statusBar.setRight("tool", "\u001B[33m⚙ " + name + "\u001B[0m");
                            app.requestRender();
                        }

                        @Override
                        public void onToolCallReady(String id, String name, JsonValue inputJson) {
                            // display already handled by onToolCallStart
                        }

                        @Override
                        public void onToolPermissionNeeded(String id, String name,
                                                           JsonValue inputJson,
                                                           Consumer<PermissionDecision> responder) {
                            // Auto-approve in agent mode (matches old behaviour)
                            responder.accept(PermissionDecision.APPROVE_ONCE);
                        }

                        @Override
                        public void onToolResult(String id, String name, ToolResult result) {
                            statusBar.remove("tool");
                            app.requestRender();
                        }

                        @Override
                        public void onUsage(int inputTokens, int outputTokens) {
                            statusBar.setRight("tokens",
                                    "\u001B[2m↑" + inputTokens + " ↓" + outputTokens + "\u001B[0m");
                            app.requestRender();
                        }

                        @Override
                        public void onDone(String stopReason) {
                            statusBar.remove("thinking");
                            // Finalise the response (remove blinking cursor placeholder)
                            if (streamed.isEmpty()) {
                                history.replaceLast(AgentMessage.assistant(agentId,
                                        "\u001B[2m(no response)\u001B[0m"));
                            } else {
                                history.replaceLast(
                                        AgentMessage.assistant(agentId, streamed.toString()));
                            }
                            stopSpinner(spinner, spinnerRunning, tickHandle, statusBar, app);
                        }

                        @Override
                        public void onError(String message) {
                            history.replaceLast(AgentMessage.assistant("system",
                                    "\u001B[31m✗ Error: " + message + "\u001B[0m"));
                            stopSpinner(spinner, spinnerRunning, tickHandle, statusBar, app);
                        }
                    });
                } catch (Exception e) {
                    history.replaceLast(AgentMessage.assistant("system",
                            "\u001B[31m✗ " + e.getMessage() + "\u001B[0m"));
                    stopSpinner(spinner, spinnerRunning, tickHandle, statusBar, app);
                }
            });
        });

        app.start();
        app.awaitStop();
    }

    // ── Spinner helpers ────────────────────────────────────────────────────────

    private static void startSpinner(SpinnerComponent spinner,
                                     AtomicBoolean running,
                                     ScheduledFuture<?>[] tickHandle,
                                     StatusBarComponent statusBar,
                                     App app) {
        if (running.compareAndSet(false, true)) {
            spinner.start("thinking…");
            statusBar.setRight("status", "\u001B[33mstreaming\u001B[0m");
            tickHandle[0] = SPINNER_TICKER.scheduleAtFixedRate(() -> {
                spinner.tick();
                app.requestRender();
            }, 80, 80, TimeUnit.MILLISECONDS);
        }
    }

    private static void stopSpinner(SpinnerComponent spinner,
                                    AtomicBoolean running,
                                    ScheduledFuture<?>[] tickHandle,
                                    StatusBarComponent statusBar,
                                    App app) {
        if (running.compareAndSet(true, false)) {
            if (tickHandle[0] != null) {
                tickHandle[0].cancel(false);
                tickHandle[0] = null;
            }
            spinner.stop();
            statusBar.remove("status");
            app.requestRender();
        }
    }

    // ── Slash-command autocomplete ─────────────────────────────────────────────

    private static CompositeAutocompleteEngine buildSlashAutocomplete() {
        record Cmd(String name, String description) {}
        List<Cmd> commands = List.of(
                new Cmd("/help",      "Show available slash commands"),
                new Cmd("/clear",     "Clear the chat history display"),
                new Cmd("/reset",     "Start a fresh conversation"),
                new Cmd("/models",    "List available models"),
                new Cmd("/model",     "Switch model  (e.g. /model gemma-4)"),
                new Cmd("/providers", "List available LLM providers"),
                new Cmd("/provider",  "Switch provider  (e.g. /provider gemini)"),
                new Cmd("/status",    "Show platform status"),
                new Cmd("/info",      "Show system information"),
                new Cmd("/projects",  "Browse and switch projects"),
                new Cmd("/sessions",  "Browse and resume sessions")
        );

        AutocompleteProvider slashProvider = new AutocompleteProvider() {

            @Override
            public List<Character> triggerCharacters() {
                return List.of('/');
            }

            @Override
            public CompletableFuture<AutocompleteSuggestions> getSuggestions(
                    List<String> lines, int cursorLine, int cursorCol, boolean force) {
                String line   = cursorLine < lines.size() ? lines.get(cursorLine) : "";
                String prefix = line.substring(0, Math.min(cursorCol, line.length()));
                if (!prefix.startsWith("/")) {
                    return CompletableFuture.completedFuture(
                            new AutocompleteSuggestions(List.of(), ""));
                }
                List<AutocompleteItem> matches = commands.stream()
                        .filter(c -> c.name().startsWith(prefix))
                        .map(c -> new AutocompleteItem(c.name(), c.name(), c.description()))
                        .toList();
                return CompletableFuture.completedFuture(
                        new AutocompleteSuggestions(matches, prefix));
            }

            @Override
            public CompletionEdit applyCompletion(List<String> lines, int cursorLine,
                                                   int cursorCol, AutocompleteItem item,
                                                   String prefix) {
                String line    = cursorLine < lines.size() ? lines.get(cursorLine) : "";
                String before  = line.substring(0, Math.max(0, cursorCol - prefix.length()));
                String after   = line.substring(Math.min(cursorCol, line.length()));
                String newLine = before + item.value() + after;
                int    newCol  = before.length() + item.value().length();
                List<String> newLines = new ArrayList<>(lines);
                if (cursorLine < newLines.size()) {
                    newLines.set(cursorLine, newLine);
                } else {
                    newLines.add(newLine);
                }
                return new CompletionEdit(newLines, cursorLine, newCol);
            }
        };

        return new CompositeAutocompleteEngine().register(slashProvider);
    }
}
