package tech.kayys.wayang.control.websocket;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import tech.kayys.wayang.control.dto.realtime.ControlPlaneRealtimeEvent;

@ApplicationScoped
public class ControlPlaneRealtimeEventObserver {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ControlPlaneWebSocketBroker broker;

    public void onRealtimeEvent(@Observes ControlPlaneRealtimeEvent event) {
        if (event == null || event.workspaceIds().isEmpty()) {
            return;
        }

        Map<String, Object> outbound = new LinkedHashMap<>();
        outbound.put("type", event.type() == null || event.type().isBlank() ? "control.event" : event.type());
        outbound.put("channel", event.channel());
        outbound.put("schemaId", event.schemaId());
        outbound.put("payload", event.payload());
        outbound.put("metadata", event.metadata());
        outbound.put("timestamp", Instant.now().toString());

        String json = toJson(outbound);
        for (String workspaceId : event.workspaceIds()) {
            broker.broadcast(workspaceId, json, null);
        }
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{\"type\":\"error\",\"message\":\"Serialization failure\"}";
        }
    }
}
