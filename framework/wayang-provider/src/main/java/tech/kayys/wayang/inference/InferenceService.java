package tech.kayys.wayang.inference;

import tech.kayys.wayang.provider.WayangPluginManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Service for routing inference requests to the appropriate InferenceProvider.
 */
public class InferenceService {

    private static final InferenceService INSTANCE = new InferenceService();
    private final WayangPluginManager pluginManager;
    private final Map<String, InferenceProvider> modelToProvider = new HashMap<>();

    public static InferenceService getInstance() {
        return INSTANCE;
    }

    private InferenceService() {
        this.pluginManager = WayangPluginManager.getInstance();
        reloadProviders();
    }

    /**
     * Reloads plugins from the plugin directory and updates the model routing table.
     */
    public synchronized void reloadProviders() {
        pluginManager.loadPlugins();
        List<InferenceProvider> providers = pluginManager.getLoadedProviders();
        modelToProvider.clear();
        for (InferenceProvider provider : providers) {
            try {
                Set<String> models = provider.listModels();
                for (String model : models) {
                    modelToProvider.put(model, provider);
                }
            } catch (Exception e) {
                // Ignore providers that fail to list models
            }
        }
    }

    /**
     * Finds a provider that supports the given model.
     */
    public InferenceProvider getProviderForModel(String model) {
        // Direct match
        InferenceProvider provider = modelToProvider.get(model);
        if (provider != null) {
            return provider;
        }

        // Prefix match (e.g. "gpt-4" matches "gpt-4-turbo")
        for (Map.Entry<String, InferenceProvider> entry : modelToProvider.entrySet()) {
            if (model.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Let's ask all loaded providers if they support it via validation
        CompletionRequest dummyReq = CompletionRequest.builder().model(model).build();
        for (InferenceProvider p : pluginManager.getLoadedProviders()) {
            if (p.validate(dummyReq)) {
                return p;
            }
        }

        throw new IllegalArgumentException("No provider found for model: " + model);
    }

    /**
     * Routes a completion request to the appropriate provider.
     */
    public CompletionResult generate(CompletionRequest request) throws Exception {
        InferenceProvider provider = getProviderForModel(request.model());
        return provider.generate(request);
    }

    /**
     * Routes a streaming completion request to the appropriate provider.
     */
    public CompletionStream stream(CompletionRequest request) throws Exception {
        InferenceProvider provider = getProviderForModel(request.model());
        if (!provider.supportsStreaming()) {
            throw new UnsupportedOperationException("Provider " + provider.metadata().name() + " does not support streaming.");
        }
        return provider.stream(request);
    }

    /**
     * Routes an async completion request to the appropriate provider.
     */
    public CompletableFuture<CompletionResult> generateAsync(CompletionRequest request) {
        try {
            InferenceProvider provider = getProviderForModel(request.model());
            return provider.generateAsync(request);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
