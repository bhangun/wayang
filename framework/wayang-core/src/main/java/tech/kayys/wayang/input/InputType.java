package tech.kayys.wayang.input;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



/**
 * Input Types
 */
public enum InputType {
    TEXT,
    VOICE,
    EMAIL,
    WHATSAPP,
    SLACK,
    DISCORD,
    REST_API,
    KAFKA,
    MQTT,
    FILE,
    SENSOR,
    EVENT_BUS,
    WEBSOCKET,
    GRPC,
    WEBHOOK
}