package tech.kayys.wayang.runtime.standalone.executor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriggerSourceExecutorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "start",
            "trigger-manual",
            "trigger-schedule",
            "trigger-email",
            "trigger-telegram",
            "trigger-websocket",
            "trigger-webhook",
            "trigger-event",
            "trigger-kafka",
            "trigger-file"
    })
    void shouldEmitTriggerPayloadForAllSupportedNodeTypes(String triggerType) {
        TriggerSourceExecutor executor = new TriggerSourceExecutor();
        WorkflowRunId runId = WorkflowRunId.of("run-1");
        NodeId nodeId = NodeId.of("node-1");
        NodeExecutionTask task = new NodeExecutionTask(
                runId,
                nodeId,
                1,
                ExecutionToken.create(runId, nodeId, 1, Duration.ofMinutes(5)),
                Map.of("__node_type__", triggerType, "timezone", "Asia/Jakarta"),
                null);

        assertTrue(executor.canHandle(task));
        var result = executor.execute(task).await().indefinitely();

        assertEquals("COMPLETED", result.status().name());
        assertNotNull(result.output());
        assertEquals(triggerType, result.output().get("triggerType"));
        assertEquals("Asia/Jakarta", result.output().get("timezone"));
        assertEquals("fired", result.output().get("triggerStatus"));
        assertNotNull(result.output().get("triggeredAt"));
    }

    @Test
    void shouldNotHandleUnsupportedNodeType() {
        TriggerSourceExecutor executor = new TriggerSourceExecutor();
        WorkflowRunId runId = WorkflowRunId.of("run-1");
        NodeId nodeId = NodeId.of("node-unsupported");
        NodeExecutionTask task = new NodeExecutionTask(
                runId,
                nodeId,
                1,
                ExecutionToken.create(runId, nodeId, 1, Duration.ofMinutes(5)),
                Map.of("__node_type__", "unknown-trigger"),
                null);

        assertFalse(executor.canHandle(task));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldApplyScheduleSchemaParameters() {
        Map<String, Object> output = executeWithConfig("trigger-schedule", Map.of(
                "mode", "cron",
                "cron", "0 */5 * * * ?",
                "timezone", "Asia/Jakarta"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("cron", integration.get("mode"));
        assertEquals("0 */5 * * * ?", integration.get("cron"));
        assertEquals("Asia/Jakarta", integration.get("timezone"));
        assertEquals("ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldApplyEmailSchemaParameters() {
        Map<String, Object> output = executeWithConfig("trigger-email", Map.of(
                "imapHost", "imap.example.com",
                "imapPort", 993,
                "folder", "INBOX",
                "subjectFilter", "URGENT"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("imap.example.com", integration.get("imapHost"));
        assertEquals(993, integration.get("imapPort"));
        assertEquals("INBOX", integration.get("folder"));
        assertEquals("URGENT", integration.get("subjectFilter"));
        assertEquals("ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldApplyTelegramSchemaParameters() {
        Map<String, Object> output = executeWithConfig("trigger-telegram", Map.of(
                "botToken", "token-123",
                "chatId", "12345",
                "allowedUserId", "u-1"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("12345", integration.get("chatId"));
        assertEquals("u-1", integration.get("allowedUserId"));
        assertEquals(true, integration.get("botTokenConfigured"));
        assertEquals("ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldApplyWebhookSchemaParameters() {
        Map<String, Object> output = executeWithConfig("trigger-webhook", Map.of(
                "path", "/hooks/orders",
                "method", "patch",
                "secret", "s3cr3t"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("/hooks/orders", integration.get("path"));
        assertEquals("PATCH", integration.get("method"));
        assertEquals(true, integration.get("secretConfigured"));
        assertEquals("ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldApplyWebsocketSchemaParameters() {
        Map<String, Object> output = executeWithConfig("trigger-websocket", Map.of(
                "path", "/ws/trigger/orders",
                "channel", "orders"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("/ws/trigger/orders", integration.get("path"));
        assertEquals("orders", integration.get("channel"));
        assertEquals("ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldApplyEventSchemaParameters() {
        Map<String, Object> output = executeWithConfig("trigger-event", Map.of(
                "eventName", "order.created",
                "eventSource", "erp"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("order.created", integration.get("eventName"));
        assertEquals("erp", integration.get("eventSource"));
        assertEquals("ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldApplyKafkaSchemaParameters() {
        Map<String, Object> output = executeWithConfig("trigger-kafka", Map.of(
                "brokers", "localhost:9092",
                "topic", "events",
                "groupId", "wayang"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("localhost:9092", integration.get("brokers"));
        assertEquals("events", integration.get("topic"));
        assertEquals("wayang", integration.get("groupId"));
        assertEquals("ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldApplyFileSchemaParameters() {
        Map<String, Object> output = executeWithConfig("trigger-file", Map.of(
                "path", "/tmp/inbox",
                "pattern", "*.json",
                "pollSeconds", 15));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("/tmp/inbox", integration.get("path"));
        assertEquals("*.json", integration.get("pattern"));
        assertEquals(15, integration.get("pollSeconds"));
        assertEquals("ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldEnableLiveModeWhenRequestedAndAllowed() {
        Map<String, Object> output = executeWithConfig("trigger-file", Map.of(
                "integrationMode", "live",
                "liveEnabled", true,
                "liveFileEnabled", true,
                "path", "/tmp",
                "pattern", "*.json",
                "pollSeconds", 5));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("live", integration.get("integrationMode"));
        assertEquals(true, integration.get("liveEnabled"));
        assertEquals("live-ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFallbackToSimulatedWhenLiveNotEnabled() {
        Map<String, Object> output = executeWithConfig("trigger-kafka", Map.of(
                "integrationMode", "live",
                "brokers", "localhost:9092",
                "topic", "events"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("simulated", integration.get("integrationMode"));
        assertEquals(false, integration.get("liveEnabled"));
        assertEquals("ready", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldMarkScheduleInvalidWhenTimezoneInvalid() {
        Map<String, Object> output = executeWithConfig("trigger-schedule", Map.of(
                "mode", "interval",
                "intervalSeconds", 30,
                "timezone", "Mars/Phobos"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals(false, integration.get("timezoneValid"));
        assertEquals("invalid-config", integration.get("status"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldMarkKafkaProbeFailedWhenProbeEnabledAndUnreachable() {
        Map<String, Object> output = executeWithConfig("trigger-kafka", Map.of(
                "integrationMode", "live",
                "liveEnabled", true,
                "liveKafkaEnabled", true,
                "liveKafkaProbeEnabled", true,
                "brokers", "127.0.0.1:1",
                "topic", "events"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("live", integration.get("integrationMode"));
        assertEquals("live-ready", integration.get("status"));
        assertEquals("failed", integration.get("probe"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSkipWebhookProbeWhenLiveNoProbeUrl() {
        Map<String, Object> output = executeWithConfig("trigger-webhook", Map.of(
                "integrationMode", "live",
                "liveEnabled", true,
                "path", "/hooks/orders",
                "method", "POST"));

        Map<String, Object> integration = (Map<String, Object>) output.get("triggerIntegration");
        assertEquals("live", integration.get("integrationMode"));
        assertEquals("live-ready", integration.get("status"));
        assertEquals("skipped", integration.get("probe"));
    }

    private static Map<String, Object> executeWithConfig(String triggerType, Map<String, Object> additionalConfig) {
        TriggerSourceExecutor executor = new TriggerSourceExecutor();
        WorkflowRunId runId = WorkflowRunId.of("run-1");
        NodeId nodeId = NodeId.of("node-1");
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("__node_type__", triggerType);
        context.putAll(additionalConfig);

        NodeExecutionTask task = new NodeExecutionTask(
                runId,
                nodeId,
                1,
                ExecutionToken.create(runId, nodeId, 1, Duration.ofMinutes(5)),
                context,
                null);

        var result = executor.execute(task).await().indefinitely();
        return result.output();
    }
}
