package tech.kayys.wayang.provider.gollek.strategy;

import tech.kayys.gollek.factory.GollekSdkFactory;
import tech.kayys.gollek.sdk.config.SdkConfig;
import tech.kayys.gollek.sdk.core.GollekSdk;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class RestGollekStrategy implements GollekStrategy {

    private final GollekSdk sdk;

    public RestGollekStrategy(String baseUrl, String apiKey) {
        try {
            this.sdk = GollekSdkFactory.createRemoteSdk(baseUrl, apiKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize REST Gollek SDK", e);
        }
    }

    @Override
    public void streamChat(List<ChatMessage> messages, String systemPrompt, List<ToolSpec> tools, double temperature, int maxTokens, Consumer<StreamEvent> onEvent) throws IOException, InterruptedException {
        // TODO: Map Wayang Provider APIs (ChatMessage/ToolSpec) to Gollek SDK APIs (GollekClient)
        // and invoke sdk.streamCompletion(...) or equivalent via REST.
        onEvent.accept(new StreamEvent.TextDelta("[REST] Stubbed response from Gollek REST."));
        onEvent.accept(new StreamEvent.MessageStop("end_turn"));
    }
}
