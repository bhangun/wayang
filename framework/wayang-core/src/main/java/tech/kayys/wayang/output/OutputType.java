package tech.kayys.wayang.spi.output;
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
 * Output Types
 */
public enum OutputType {
    TEXT,
    VOICE,
    EMAIL,
    API,
    PDF,
    DATABASE,
    SLACK,
    WHATSAPP,
    DISCORD,
    EVENT_BUS,
    KAFKA,
    MQTT,
    WEBSOCKET,
    FILE,
    CONSOLE,
    TEAMS,
    TELEGRAM
}
