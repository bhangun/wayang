package tech.kayys.wayang.provider;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.Set;
import tech.kayys.wayang.resource.Modality;

/**
 * A pluggable AI backend. Implementations translate the normalized
 * {@link ChatMessage}/{@link ToolSpec} model into their own wire format,
 * stream the HTTP response, and emit normalized {@link StreamEvent}s.
 */
public interface Provider {

    String id();

    /**
     * Streams a chat completion. Blocks the calling thread until the
     * stream ends or an error occurs; callers typically invoke this on
     * a background thread and hop back to the UI thread inside onEvent.
     */
    void streamChat(
            List<ChatMessage> messages,
            String systemPrompt,
            List<ToolSpec> tools,
            double temperature,
            int maxTokens,
            Consumer<StreamEvent> onEvent
    ) throws IOException, InterruptedException;
    
    /**
     * @return the set of modalities this provider supports (defaults to TEXT only)
     */
    default Set<Modality> supportedModalities() {
        return Set.of(Modality.TEXT);
    }
}
