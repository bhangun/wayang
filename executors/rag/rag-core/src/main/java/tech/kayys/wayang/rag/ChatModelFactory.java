
@ApplicationScoped
class ChatModelFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ChatModelFactory.class);

    public ChatLanguageModel createModel(
            String provider, String model, String apiKey,
            double temperature, int maxTokens) {

        LOG.info("Creating chat model: provider={}, model={}", provider, model);

        return switch (provider.toLowerCase()) {
            case "openai" -> OpenAiChatModel.builder()
                    .apiKey(apiKey != null ? apiKey : System.getenv("OPENAI_API_KEY"))
                    .modelName(model)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .timeout(Duration.ofSeconds(60))
                    .logRequests(false)
                    .logResponses(false)
                    .build();

            case "anthropic" -> AnthropicChatModel.builder()
                    .apiKey(apiKey != null ? apiKey : System.getenv("ANTHROPIC_API_KEY"))
                    .modelName(model)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .timeout(Duration.ofSeconds(60))
                    .build();

            default -> throw new IllegalArgumentException(
                    "Unsupported chat model provider: " + provider);
        };
    }
}
