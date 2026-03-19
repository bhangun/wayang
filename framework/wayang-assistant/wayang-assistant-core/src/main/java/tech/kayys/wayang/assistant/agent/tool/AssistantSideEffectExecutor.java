package tech.kayys.wayang.assistant.agent.tool;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.wayang.assistant.agent.ConversationSession;
import tech.kayys.wayang.tool.spi.Tool;

import java.util.Map;

/**
 * Executes side effects requested by the Assistant.
 */
@ApplicationScoped
public class AssistantSideEffectExecutor {

    private static final Logger LOG = Logger.getLogger(AssistantSideEffectExecutor.class);

    @Inject
    Instance<Tool> assistantTools;

    public void executeSlackReport(String message, ConversationSession session) {
        LOG.info("Executing autonomous Slack bug report side-effect.");
        Map<String, Object> args = Map.of(
            "bugDescription", "User reported issue: " + message,
            "environment", "Production / Live Session",
            "reproSteps", "Reported during chat session: " + session.getSessionId()
        );
        
        if (assistantTools != null) {
            assistantTools.stream()
                .filter(t -> "slack-bug-report".equals(t.id()))
                .findFirst()
                .ifPresent(t -> t.execute(args, Map.of()).subscribe().with(
                    res -> LOG.infof("Slack report sent autonomously: %s", res),
                    err -> LOG.error("Autonomous Slack report failed", err)
                ));
        }
    }
}
