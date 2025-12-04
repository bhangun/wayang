/**
 * Event bus for workflow events
 */
@ApplicationScoped
public class EventBus {
    
    @Inject
    @Channel("workflow-events")
    Emitter<String> eventEmitter;
    
    public void publish(String eventType, Map<String, Object> payload) {
        var event = Map.of(
            "type", eventType,
            "payload", payload,
            "timestamp", Instant.now()
        );
        
        eventEmitter.send(JsonUtils.toJson(event));
    }
}
