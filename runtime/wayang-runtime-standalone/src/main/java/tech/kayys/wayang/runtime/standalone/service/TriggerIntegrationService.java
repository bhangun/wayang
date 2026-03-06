package tech.kayys.wayang.runtime.standalone.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class TriggerIntegrationService {
    private static final Pattern SIMPLE_CRON = Pattern.compile("^\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+(\\s+\\S+)?$");
    private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(2);

    public Map<String, Object> enrich(String triggerType, String nodeId, Map<String, Object> context) {
        Map<String, Object> integration = new LinkedHashMap<>();
        boolean liveRequested = "live".equalsIgnoreCase(stringValue(context.get("integrationMode"), "simulated"));
        boolean globalLiveEnabled = isEnabled(
                "wayang.trigger.integration.live.enabled",
                context,
                "liveEnabled",
                false);
        boolean liveEnabled = liveRequested && globalLiveEnabled;
        integration.put("integrationMode", liveEnabled ? "live" : "simulated");
        integration.put("liveRequested", liveRequested);
        integration.put("liveEnabled", liveEnabled);

        switch (triggerType) {
            case "start":
                integration.put("source", "workflow-start");
                integration.put("status", "ready");
                return integration;
            case "trigger-manual":
                integration.put("source", "manual");
                integration.put("allowApiStart", boolValue(context.get("allowApiStart"), true));
                integration.put("status", "ready");
                return integration;
            case "trigger-schedule":
                return scheduleIntegration(integration, context, liveEnabled);
            case "trigger-email":
                return emailIntegration(integration, context, liveEnabled);
            case "trigger-telegram":
                return telegramIntegration(integration, context, liveEnabled);
            case "trigger-websocket":
                return websocketIntegration(integration, context, nodeId, liveEnabled);
            case "trigger-webhook":
                return webhookIntegration(integration, context, nodeId, liveEnabled);
            case "trigger-event":
                return eventIntegration(integration, context, liveEnabled);
            case "trigger-kafka":
                return kafkaIntegration(integration, context, liveEnabled);
            case "trigger-file":
                return fileIntegration(integration, context, liveEnabled);
            default:
                integration.put("status", "unsupported");
                integration.put("reason", "Unsupported trigger type: " + triggerType);
                return integration;
        }
    }

    private static Map<String, Object> scheduleIntegration(
            Map<String, Object> integration,
            Map<String, Object> context,
            boolean liveEnabled) {
        String mode = stringValue(context.get("mode"), "interval").toLowerCase(Locale.ROOT);
        String timezone = stringValue(context.get("timezone"), "UTC");
        integration.put("mode", mode);
        integration.put("timezone", timezone);
        boolean timezoneValid = isValidTimezone(timezone);
        integration.put("timezoneValid", timezoneValid);
        if ("cron".equals(mode)) {
            String cron = stringValue(context.get("cron"), "");
            integration.put("cron", cron);
            integration.put("cronValid", SIMPLE_CRON.matcher(cron).matches());
            integration.put("configured", !cron.isBlank() && boolValue(integration.get("cronValid"), false));
        } else {
            int intervalSeconds = intValue(context.get("intervalSeconds"), 60);
            integration.put("intervalSeconds", intervalSeconds);
            integration.put("nextFireAt", Instant.now().plusSeconds(intervalSeconds).toString());
            integration.put("configured", intervalSeconds > 0);
        }
        if (!boolValue(integration.get("configured"), false)) {
            integration.put("status", "missing-config");
        } else if (!timezoneValid) {
            integration.put("status", "invalid-config");
        } else if (liveEnabled) {
            integration.put("status", "live-ready");
        } else {
            integration.put("status", "ready");
        }
        return integration;
    }

    private static Map<String, Object> emailIntegration(
            Map<String, Object> integration,
            Map<String, Object> context,
            boolean liveEnabled) {
        String host = stringValue(context.get("imapHost"), "");
        int port = intValue(context.get("imapPort"), 993);
        String folder = stringValue(context.get("folder"), "INBOX");
        String subjectFilter = stringValue(context.get("subjectFilter"), "");
        integration.put("imapHost", host);
        integration.put("imapPort", port);
        integration.put("folder", folder);
        integration.put("subjectFilter", subjectFilter);
        integration.put("configured", !host.isBlank());
        if (!boolValue(integration.get("configured"), false)) {
            integration.put("status", "missing-config");
        } else if (liveEnabled && isEnabled("wayang.trigger.integration.live.email.enabled", context, "liveEmailEnabled", false)) {
            applyTcpProbe(integration, host, port, context, "liveEmailProbeEnabled");
            integration.put("status", "live-ready");
        } else {
            integration.put("status", "ready");
        }
        return integration;
    }

    private static Map<String, Object> telegramIntegration(
            Map<String, Object> integration,
            Map<String, Object> context,
            boolean liveEnabled) {
        String botToken = stringValue(context.get("botToken"), "");
        String chatId = stringValue(context.get("chatId"), "");
        String allowedUserId = stringValue(context.get("allowedUserId"), "");
        integration.put("botTokenConfigured", !botToken.isBlank());
        integration.put("chatId", chatId);
        integration.put("allowedUserId", allowedUserId);
        integration.put("configured", !botToken.isBlank() && !chatId.isBlank());
        if (!boolValue(integration.get("configured"), false)) {
            integration.put("status", "missing-config");
        } else if (liveEnabled
                && isEnabled("wayang.trigger.integration.live.telegram.enabled", context, "liveTelegramEnabled", false)) {
            integration.put("status", "live-ready");
            integration.put("apiEndpoint", "https://api.telegram.org");
            applyHttpProbe(
                    integration,
                    "https://api.telegram.org/bot" + botToken + "/getMe",
                    context,
                    "liveTelegramProbeEnabled");
        } else {
            integration.put("status", "ready");
        }
        return integration;
    }

    private static Map<String, Object> websocketIntegration(
            Map<String, Object> integration,
            Map<String, Object> context,
            String nodeId,
            boolean liveEnabled) {
        String path = stringValue(context.get("path"), "/ws/triggers/" + nodeId);
        String channel = stringValue(context.get("channel"), nodeId);
        integration.put("path", path);
        integration.put("channel", channel);
        integration.put("configured", true);
        integration.put("status", liveEnabled ? "live-ready" : "ready");
        return integration;
    }

    private static Map<String, Object> webhookIntegration(
            Map<String, Object> integration,
            Map<String, Object> context,
            String nodeId,
            boolean liveEnabled) {
        String path = stringValue(context.get("path"), "/webhooks/" + nodeId);
        String method = stringValue(context.get("method"), "POST").toUpperCase(Locale.ROOT);
        String secret = stringValue(context.get("secret"), "");
        integration.put("path", path);
        integration.put("method", method);
        integration.put("secretConfigured", !secret.isBlank());
        integration.put("configured", !path.isBlank());
        integration.put("status", liveEnabled ? "live-ready" : "ready");
        if (liveEnabled) {
            String probeUrl = stringValue(context.get("probeUrl"), "");
            if (!probeUrl.isBlank()) {
                applyHttpProbe(integration, probeUrl, context, "liveWebhookProbeEnabled");
            } else {
                integration.put("probe", "skipped");
            }
        }
        return integration;
    }

    private static Map<String, Object> eventIntegration(
            Map<String, Object> integration,
            Map<String, Object> context,
            boolean liveEnabled) {
        integration.put("eventName", stringValue(context.get("eventName"), "generic-event"));
        integration.put("eventSource", stringValue(context.get("eventSource"), "internal"));
        integration.put("configured", true);
        integration.put("status", liveEnabled ? "live-ready" : "ready");
        return integration;
    }

    private static Map<String, Object> kafkaIntegration(
            Map<String, Object> integration,
            Map<String, Object> context,
            boolean liveEnabled) {
        String brokers = stringValue(context.get("brokers"), "");
        String topic = stringValue(context.get("topic"), "");
        String groupId = stringValue(context.get("groupId"), "wayang-trigger-group");
        integration.put("brokers", brokers);
        integration.put("topic", topic);
        integration.put("groupId", groupId);
        integration.put("brokerCount", brokers.isBlank() ? 0 : brokers.split(",").length);
        integration.put("configured", !brokers.isBlank() && !topic.isBlank());
        if (!boolValue(integration.get("configured"), false)) {
            integration.put("status", "missing-config");
        } else if (liveEnabled && isEnabled("wayang.trigger.integration.live.kafka.enabled", context, "liveKafkaEnabled", false)) {
            String firstBroker = brokers.split(",")[0].trim();
            String[] hostPort = firstBroker.split(":");
            if (hostPort.length == 2) {
                int port = intValue(hostPort[1], 9092);
                applyTcpProbe(integration, hostPort[0], port, context, "liveKafkaProbeEnabled");
            } else {
                integration.put("probe", "invalid-broker");
            }
            integration.put("status", "live-ready");
        } else {
            integration.put("status", "ready");
        }
        return integration;
    }

    private static Map<String, Object> fileIntegration(
            Map<String, Object> integration,
            Map<String, Object> context,
            boolean liveEnabled) {
        String path = stringValue(context.get("path"), "");
        String pattern = stringValue(context.get("pattern"), "*");
        int pollSeconds = intValue(context.get("pollSeconds"), 30);
        integration.put("path", path);
        integration.put("pattern", pattern);
        integration.put("pollSeconds", pollSeconds);
        if (!path.isBlank()) {
            Path triggerPath = Paths.get(path);
            integration.put("pathExists", Files.exists(triggerPath));
            integration.put("pathReadable", Files.isReadable(triggerPath));
        }
        integration.put("configured", !path.isBlank());
        if (!boolValue(integration.get("configured"), false)) {
            integration.put("status", "missing-config");
        } else if (liveEnabled && isEnabled("wayang.trigger.integration.live.file.enabled", context, "liveFileEnabled", true)) {
            integration.put("status", "live-ready");
        } else {
            integration.put("status", "ready");
        }
        return integration;
    }

    private static boolean isEnabled(
            String propertyName,
            Map<String, Object> context,
            String contextKey,
            boolean fallback) {
        Object contextValue = context.get(contextKey);
        if (contextValue != null) {
            return boolValue(contextValue, fallback);
        }
        Optional<Boolean> cfg = ConfigProvider.getConfig().getOptionalValue(propertyName, Boolean.class);
        return cfg.orElse(fallback);
    }

    private static boolean isValidTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
            return true;
        } catch (DateTimeException ignored) {
            return false;
        }
    }

    private static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String raw = String.valueOf(value).trim();
        return raw.isEmpty() ? fallback : raw;
    }

    private static int intValue(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean boolValue(Object value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private static void applyTcpProbe(
            Map<String, Object> integration,
            String host,
            int port,
            Map<String, Object> context,
            String contextProbeFlag) {
        boolean probeEnabled = boolValue(
                context.get(contextProbeFlag),
                isEnabled("wayang.trigger.integration.live.probe.enabled", context, "liveProbeEnabled", false));
        if (!probeEnabled) {
            integration.put("probe", "skipped");
            return;
        }
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new InetSocketAddress(host, port), (int) PROBE_TIMEOUT.toMillis());
            integration.put("probe", "ok");
        } catch (Exception e) {
            integration.put("probe", "failed");
            integration.put("probeError", e.getClass().getSimpleName());
        }
    }

    private static void applyHttpProbe(
            Map<String, Object> integration,
            String url,
            Map<String, Object> context,
            String contextProbeFlag) {
        boolean probeEnabled = boolValue(
                context.get(contextProbeFlag),
                isEnabled("wayang.trigger.integration.live.probe.enabled", context, "liveProbeEnabled", false));
        if (!probeEnabled) {
            integration.put("probe", "skipped");
            return;
        }
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(PROBE_TIMEOUT)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(PROBE_TIMEOUT)
                    .GET()
                    .build();
            int code = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            integration.put("probeHttpStatus", code);
            integration.put("probe", code >= 200 && code < 500 ? "ok" : "failed");
        } catch (Exception e) {
            integration.put("probe", "failed");
            integration.put("probeError", e.getClass().getSimpleName());
        }
    }
}
