package tech.kayys.wayang.agent.orchestration.graph.resume;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import tech.kayys.wayang.spi.agent.Agent;

import java.util.logging.Logger;

/**
 * Listens for HITL TaskApprovedEvent JSON on the Vert.x EventBus and
 * automatically resumes the corresponding Agent thread.
 *
 * <p>This is a plain POJO — no CDI annotations — so it can be instantiated
 * from WayangServe (plain main) or from a Quarkus startup bean.</p>
 */
public class VertxAutoResumeListener {

    private static final Logger LOGGER = Logger.getLogger(VertxAutoResumeListener.class.getName());
    public static final String HITL_APPROVED_ADDRESS = "wayang.hitl.task.approved";

    private Vertx vertx;

    /** No-arg constructor for programmatic instantiation. */
    public VertxAutoResumeListener() {}

    /** Constructor for injection-style usage. */
    public VertxAutoResumeListener(Vertx vertx) {
        this.vertx = vertx;
    }

    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Registers the EventBus consumer. Call this once the Vertx instance
     * has been set (either via constructor or {@link #setVertx}).
     *
     * @param ev ignored startup event argument (may be null when called manually)
     */
    public void onStart(Object ev) {
        if (vertx == null) {
            LOGGER.severe("Cannot start VertxAutoResumeListener: Vertx instance is null.");
            return;
        }
        vertx.eventBus().<JsonObject>consumer(HITL_APPROVED_ADDRESS, this::handleTaskApproved);
        LOGGER.info("Started Vert.x AutoResumeListener for HITL on address: " + HITL_APPROVED_ADDRESS);
    }

    private void handleTaskApproved(Message<JsonObject> message) {
        JsonObject event = message.body();
        if (event == null || !event.containsKey("taskId")) {
            LOGGER.warning("Received invalid TaskApprovedEvent JSON");
            return;
        }

        // taskId may be a plain string or a nested {"value":"..."} object
        Object taskIdObj = event.getValue("taskId");
        String taskId;
        if (taskIdObj instanceof JsonObject nested) {
            taskId = nested.getString("value");
        } else {
            taskId = String.valueOf(taskIdObj);
        }

        if (taskId == null || taskId.equals("null")) {
            LOGGER.warning("taskId could not be resolved from event JSON");
            return;
        }

        String threadId = tech.kayys.wayang.agent.orchestration.graph.MultiAgent.getThreadIdForTask(taskId);
        if (threadId == null) {
            LOGGER.warning("No paused thread found for taskId: " + taskId + ". Skipping auto-resume.");
            return;
        }

        LOGGER.info("Auto-resuming agent for threadId: " + threadId + " (taskId: " + taskId + ")");
        try {
            tech.kayys.wayang.agent.orchestration.graph.MultiAgent.resumeThread(threadId);
        } catch (Exception e) {
            LOGGER.severe("Failed to auto-resume agent for threadId: " + threadId + ". Error: " + e.getMessage());
        }
    }
}
