package tech.kayys.wayang.trigger;
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
 * Trigger Types
 */
public enum TriggerType {
    CRON,
    WEBHOOK,
    KAFKA,
    RABBITMQ,
    MQTT,
    FILE,
    EMAIL,
    REST,
    SCHEDULE,
    MANUAL,
    EVENT,
    TIMER,
    SENSOR,
    WEB_SOCKET
}
