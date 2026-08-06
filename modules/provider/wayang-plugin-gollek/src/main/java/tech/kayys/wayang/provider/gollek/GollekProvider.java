package tech.kayys.wayang.provider.gollek;

import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.Provider;
import tech.kayys.wayang.provider.ProviderStrategy;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.provider.gollek.strategy.EmbeddedGollekStrategy;
import tech.kayys.wayang.provider.gollek.strategy.GollekStrategy;
import tech.kayys.wayang.provider.gollek.strategy.GrpcGollekStrategy;
import tech.kayys.wayang.provider.gollek.strategy.RestGollekStrategy;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Wayang Provider implementation for Gollek.
 * Delegates the actual invocation to a {@link GollekStrategy} based on configuration.
 */
public class GollekProvider implements Provider {

    private final GollekStrategy strategy;

    public GollekProvider(ProviderStrategy strategyType, String baseUrl, String apiKey) {
        this.strategy = switch (strategyType) {
            case EMBEDDED -> new EmbeddedGollekStrategy();
            case REST -> new RestGollekStrategy(baseUrl, apiKey);
            case GRPC -> new GrpcGollekStrategy(baseUrl);
            default -> throw new IllegalArgumentException("Unsupported strategy: " + strategyType);
        };
    }

    @Override
    public String id() {
        return "gollek";
    }

    @Override
    public void streamChat(List<ChatMessage> messages, String systemPrompt, List<ToolSpec> tools, double temperature, int maxTokens, Consumer<StreamEvent> onEvent) throws IOException, InterruptedException {
        strategy.streamChat(messages, systemPrompt, tools, temperature, maxTokens, onEvent);
    }
}
