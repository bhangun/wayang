package tech.kayys.wayang.eip.client;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.kayys.wayang.eip.config.EndpointConfig;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Real Kafka Endpoint Client Implementation
 */
@ApplicationScoped
class KafkaEndpointClient implements EndpointClient {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEndpointClient.class);

    @Inject
    @Channel("dynamic-kafka-out")
    Emitter<String> kafkaEmitter;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Uni<Object> send(EndpointConfig config, Object payload) {
        try {
            // Extract topic from URI (kafka://topic-name)
            String topic = config.uri().replace("kafka://", "");

            // Serialize payload
            String jsonPayload = payload instanceof String
                    ? (String) payload
                    : objectMapper.writeValueAsString(payload);

            // Create message with metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("topic", topic);
            metadata.put("timestamp", Instant.now().toString());
            config.headers().forEach(metadata::put);

            // Create message
            Message<String> message = Message.of(jsonPayload)
                    .addMetadata(metadata);

            // Send to Kafka
            kafkaEmitter.send(message);

            return Uni.createFrom().item(Map.of(
                    "topic", topic,
                    "sent", true,
                    "timestamp", Instant.now().toString()));

        } catch (Exception e) {
            LOG.error("Kafka send failed", e);
            return Uni.createFrom().failure(e);
        }
    }
}