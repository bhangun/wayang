# Inference Provider Guide

Wayang is designed to be agnostic to the underlying LLM inference provider. While **Gollek** is the default, highly-optimized backend for the platform, you can seamlessly integrate third-party APIs (like OpenAI, Gemini, or Claude).

## 1. The Provider SPI

To create a new provider, you must implement the inference strategies defined in `wayang-spi`.

```java
package tech.kayys.wayang.provider.custom;

import tech.kayys.wayang.provider.ProviderStrategy;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Consumer;

@ApplicationScoped
public class CustomRestProviderStrategy implements ProviderStrategy {

    private final HttpClient httpClient;
    private final String endpointUrl = "https://api.custom-cloud.com/v1/chat/completions";
    private final String apiKey = System.getenv("CUSTOM_API_KEY");

    public CustomRestProviderStrategy() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String getProviderId() {
        return "custom-cloud";
    }

    @Override
    public void streamChat(List<ChatMessage> messages, 
                           String systemPrompt, 
                           List<ToolSpec> tools, 
                           double temperature, 
                           int maxTokens, 
                           Consumer<StreamEvent> onEvent) throws Exception {
        
        // 1. Construct the JSON Request payload manually or using Jackson
        StringBuilder jsonPayload = new StringBuilder();
        jsonPayload.append("{");
        jsonPayload.append("\"model\": \"latest-model-v1\", ");
        jsonPayload.append("\"temperature\": ").append(temperature).append(", ");
        jsonPayload.append("\"stream\": true, ");
        jsonPayload.append("\"messages\": [");
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            jsonPayload.append("{\"role\": \"system\", \"content\": \"").append(systemPrompt).append("\"},");
        }
        
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            String role = msg.role == ChatMessage.Role.ASSISTANT ? "assistant" : "user";
            jsonPayload.append("{\"role\": \"").append(role).append("\", \"content\": \"").append(msg.textOnly()).append("\"}");
            if (i < messages.size() - 1) jsonPayload.append(",");
        }
        jsonPayload.append("]}");

        // 2. Build the HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload.toString()))
                .build();

        // 3. Execute request and stream lines asynchronously
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
            .thenAccept(response -> {
                if (response.statusCode() != 200) {
                    onEvent.accept(new StreamEvent.Error("HTTP Error: " + response.statusCode()));
                    return;
                }
                
                // 4. Parse Server-Sent Events (SSE) stream
                response.body().forEach(line -> {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if (data.equals("[DONE]")) {
                            onEvent.accept(new StreamEvent.MessageStop("stop"));
                        } else {
                            // In a real scenario, use Jackson to parse `data` into JSON and extract delta text
                            String textDelta = extractDeltaFromJson(data); 
                            onEvent.accept(new StreamEvent.TextDelta(textDelta));
                        }
                    }
                });
            }).join(); // Wait for stream to finish
    }

    private String extractDeltaFromJson(String jsonLine) {
        // Dummy parsing logic
        return " (token) "; 
    }
}
```

## 2. Configuration
Users can activate your provider by setting it in their `~/.wayang/config.yaml`:

```yaml
provider: custom-cloud
custom-cloud:
  api.key: sk-...
  model: latest-model-v1
```

Wayang's CLI initialization will read this config and dynamically route inference requests to your `CustomProviderStrategy`.
