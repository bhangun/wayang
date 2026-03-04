package tech.kayys.wayang.control.websocket;

import java.util.Map;

public class ControlPlaneSocketMessage {
    public String type;
    public String correlationId;
    public String channel;
    public String schemaId;
    public Map<String, Object> payload;
    public Map<String, Object> metadata;
}
