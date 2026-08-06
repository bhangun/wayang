package tech.kayys.wayang.provider.gollek.strategy;

import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class GrpcGollekStrategy implements GollekStrategy {

    private final String grpcTarget;

    public GrpcGollekStrategy(String grpcTarget) {
        this.grpcTarget = grpcTarget;
        // TODO: Initialize gRPC Channel and Stubs when InferenceService is available in gollek-sdk-protobuf
    }

    @Override
    public void streamChat(List<ChatMessage> messages, String systemPrompt, List<ToolSpec> tools, double temperature, int maxTokens, Consumer<StreamEvent> onEvent) throws IOException, InterruptedException {
        // TODO: Map Wayang Provider APIs (ChatMessage/ToolSpec) to Gollek protobuf definitions
        // and invoke inferenceStub.streamChat(...) via gRPC.
        onEvent.accept(new StreamEvent.TextDelta("[gRPC] Stubbed response from Gollek gRPC."));
        onEvent.accept(new StreamEvent.MessageStop("end_turn"));
    }
}
