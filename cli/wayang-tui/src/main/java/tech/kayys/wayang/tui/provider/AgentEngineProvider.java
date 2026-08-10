package tech.kayys.wayang.tui.provider;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import tech.kayys.wayang.api.grpc.CodeChunk;
import tech.kayys.wayang.api.grpc.CodeRequest;
import tech.kayys.wayang.api.grpc.CodeServiceGrpc;
import tech.kayys.wayang.sdk.provider.ChatMessage;
import tech.kayys.wayang.sdk.provider.StreamEvent;
import tech.kayys.wayang.sdk.provider.Provider;
import tech.kayys.wayang.sdk.provider.ToolSpec;

import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.CountDownLatch;

/**
 * A provider that delegates to the remote Wayang Agent Engine (CodeService).
 * It bridges the streaming CLI UI to a backend gRPC endpoint.
 */
public class AgentEngineProvider implements Provider {

    private final String strategy;
    private final ManagedChannel channel;
    private final CodeServiceGrpc.CodeServiceStub stub;

    public AgentEngineProvider(String baseUrl, String strategy) {
        this.strategy = strategy != null ? strategy : "tdd";
        String target = System.getenv("WAYANG_GRPC_ENDPOINT");
        if (target == null) target = "localhost:31013";
        else if (target.startsWith("http://")) target = target.substring(7);
        
        this.channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        this.stub = CodeServiceGrpc.newStub(channel);
    }

    @Override
    public String id() {
        return "engine";
    }

    @Override
    public void streamChat(List<ChatMessage> history, String systemPrompt, List<ToolSpec> tools, double temperature, int maxTokens, Consumer<StreamEvent> onEvent) {
        onEvent.accept(new StreamEvent.TextDelta("[Delegating to Wayang Agent Engine with strategy: " + strategy + "]\n"));
        onEvent.accept(new StreamEvent.ThinkingDelta("Initializing gRPC execution strategy..."));

        String prompt = history.isEmpty() ? "Hello" : history.get(history.size() - 1).textOnly();
        if (prompt == null || prompt.isBlank()) prompt = "Continue";

        CodeRequest request = CodeRequest.newBuilder()
                .setProjectId("default")
                .setSessionId("cli-session")
                .setPrompt(prompt)
                .setModel(strategy)
                .setOnce(false)
                .build();

        onEvent.accept(new StreamEvent.ThinkingDelta("\nExecuting strategy... this may take some time as it streams from background."));

        CountDownLatch latch = new CountDownLatch(1);

        stub.streamCode(request, new StreamObserver<CodeChunk>() {
            boolean first = true;
            @Override
            public void onNext(CodeChunk chunk) {
                if (first) {
                    onEvent.accept(new StreamEvent.ThinkingEnd());
                    first = false;
                }
                // Text comes as delta
                onEvent.accept(new StreamEvent.TextDelta(chunk.getText()));
            }

            @Override
            public void onError(Throwable t) {
                if (first) onEvent.accept(new StreamEvent.ThinkingEnd());
                onEvent.accept(new StreamEvent.Error("gRPC Error: " + t.getMessage()));
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                if (first) onEvent.accept(new StreamEvent.ThinkingEnd());
                onEvent.accept(new StreamEvent.MessageStop("end_turn"));
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

