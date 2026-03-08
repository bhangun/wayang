package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.ws.rs.core.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class ProjectsExecutionLifecycleSupport {
    private ProjectsExecutionLifecycleSupport() {
    }

    static Response transitionError(String executionId, String fromStatus, String toStatus, String errorCode) {
        return errorResponse(
                Response.Status.CONFLICT,
                errorCode,
                "Invalid execution status transition",
                false,
                Map.of(
                        "executionId", executionId,
                        "fromStatus", fromStatus,
                        "toStatus", toStatus),
                retryAfterSeconds(2L));
    }

    static Response errorResponse(
            Response.Status status,
            String code,
            String message,
            boolean retryable,
            Map<String, Object> details,
            long retryAfterSeconds) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("errorCode", code);
        payload.put("message", message);
        payload.put("httpStatus", status.getStatusCode());
        payload.put("retryable", retryable);
        payload.put("timestamp", Instant.now().toString());
        payload.put("details", details != null ? details : Map.of());
        final Response.ResponseBuilder builder = Response.status(status).entity(payload);
        if (retryable) {
            payload.put("retryAfterSeconds", retryAfterSeconds);
            builder.header("Retry-After", String.valueOf(retryAfterSeconds));
        }
        return builder.build();
    }

    static Response.ResponseBuilder addRateLimitHeaders(
            Response.ResponseBuilder builder,
            RateLimitDecision rateLimit) {
        if (builder == null || rateLimit == null) {
            return builder;
        }
        builder.header("X-RateLimit-Limit", String.valueOf(rateLimit.limit()));
        builder.header("X-RateLimit-Remaining", String.valueOf(Math.max(0L, rateLimit.remaining())));
        builder.header("X-RateLimit-Reset", String.valueOf(rateLimit.resetEpochSeconds()));
        return builder;
    }

    static Response rateLimitedResponse(RateLimitDecision rateLimit, String errorCode) {
        final long retryAfter = Math.max(1L, rateLimit.retryAfterSeconds());
        final Map<String, Object> details = new LinkedHashMap<>();
        details.put("limit", rateLimit.limit());
        details.put("remaining", 0L);
        details.put("resetEpochSeconds", rateLimit.resetEpochSeconds());
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("errorCode", errorCode);
        payload.put("message", "Execution submit rate limit exceeded");
        payload.put("httpStatus", Response.Status.TOO_MANY_REQUESTS.getStatusCode());
        payload.put("retryable", true);
        payload.put("retryAfterSeconds", retryAfter);
        payload.put("timestamp", Instant.now().toString());
        payload.put("details", details);
        return addRateLimitHeaders(
                Response.status(Response.Status.TOO_MANY_REQUESTS)
                        .header("Retry-After", String.valueOf(retryAfter))
                        .entity(payload),
                rateLimit).build();
    }

    static Response backpressureResponse(
            RateLimitDecision rateLimit,
            String errorCode,
            long retryAfter,
            int maxInFlight,
            int currentInFlight) {
        final Map<String, Object> details = new LinkedHashMap<>();
        details.put("maxInFlight", maxInFlight);
        details.put("currentInFlight", currentInFlight);
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("errorCode", errorCode);
        payload.put("message", "Execution submit backpressure: too many in-flight submits");
        payload.put("httpStatus", Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        payload.put("retryable", true);
        payload.put("retryAfterSeconds", retryAfter);
        payload.put("timestamp", Instant.now().toString());
        payload.put("details", details);
        return addRateLimitHeaders(
                Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .header("Retry-After", String.valueOf(retryAfter))
                        .entity(payload),
                rateLimit).build();
    }

    static Map<String, Object> errorDetails(String executionId, Exception e) {
        final Map<String, Object> details = new LinkedHashMap<>();
        details.put("executionId", executionId);
        final String detail = optionalStringValue(e != null ? e.getMessage() : null);
        if (detail != null) {
            details.put("detail", detail);
        }
        return details;
    }

    static String resolveStopReason(Object rawReason, Set<String> supportedStopReasons, String defaultReason) {
        final String provided = optionalStringValue(rawReason);
        if (provided == null) {
            return defaultReason;
        }
        final String normalized = provided.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return supportedStopReasons.contains(normalized) ? normalized : null;
    }

    static Long resolveExpectedVersion(String ifMatch, Object expectedVersionRaw) {
        final Long headerVersion = parseIfMatchVersion(ifMatch);
        if (headerVersion != null) {
            return headerVersion;
        }
        if (expectedVersionRaw == null) {
            return null;
        }
        if (expectedVersionRaw instanceof Number numeric) {
            return numeric.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(expectedVersionRaw).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    static boolean etagEquals(String ifNoneMatch, String currentEtag) {
        final String raw = optionalStringValue(ifNoneMatch);
        if (raw == null || currentEtag == null) {
            return false;
        }
        if ("*".equals(raw.trim())) {
            return true;
        }
        final String[] candidates = raw.split(",");
        for (String candidate : candidates) {
            if (candidate != null && normalizeEtagValue(candidate).equals(normalizeEtagValue(currentEtag))) {
                return true;
            }
        }
        return false;
    }

    static String executionVersionEtag(Map<String, Object> execution) {
        return String.valueOf(executionVersion(execution));
    }

    static long executionVersion(Map<String, Object> execution) {
        return longValue(execution != null ? execution.get("version") : null, 1L);
    }

    static void bumpExecutionVersion(Map<String, Object> execution) {
        if (execution == null) {
            return;
        }
        execution.put("version", executionVersion(execution) + 1L);
    }

    static Response validateExpectedVersion(
            Map<String, Object> execution,
            long expectedVersion,
            String executionId,
            String errorCode,
            long retryAfterSeconds) {
        final long currentVersion = executionVersion(execution);
        if (expectedVersion == currentVersion) {
            return null;
        }
        return errorResponse(
                Response.Status.CONFLICT,
                errorCode,
                "Execution version conflict",
                true,
                Map.of(
                        "executionId", executionId,
                        "expectedVersion", expectedVersion,
                        "currentVersion", currentVersion),
                retryAfterSeconds);
    }

    static boolean isIdempotencyEnabled() {
        return booleanValue(System.getProperty("wayang.runtime.standalone.execution.idempotency.enabled", "true"));
    }

    static long idempotencyReplayWindowSeconds(long defaultReplayWindowSeconds) {
        final String raw = System.getProperty(
                "wayang.runtime.standalone.execution.idempotency.replay-window-seconds",
                String.valueOf(defaultReplayWindowSeconds));
        try {
            return Long.parseLong(raw.trim());
        } catch (Exception ignored) {
            return defaultReplayWindowSeconds;
        }
    }

    static boolean isWithinIdempotencyReplayWindow(
            Map<String, Object> execution,
            Instant now,
            long replayWindowSeconds) {
        if (replayWindowSeconds <= 0) {
            return false;
        }
        final Instant createdAt = parseInstantOrEpoch(execution.get("createdAt"));
        final long age = Math.max(0L, Duration.between(createdAt, now).getSeconds());
        return age <= replayWindowSeconds;
    }

    static Instant parseInstantOrEpoch(Object raw) {
        final String value = optionalStringValue(raw);
        if (value == null) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            return Instant.EPOCH;
        }
    }

    static long longValue(Object raw, long fallback) {
        if (raw == null) {
            return fallback;
        }
        if (raw instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(raw.toString().trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static long retryAfterSeconds(long defaultRetryAfterSeconds) {
        final String raw = System.getProperty(
                "wayang.runtime.standalone.execution.retry-after-seconds",
                String.valueOf(defaultRetryAfterSeconds));
        try {
            final long parsed = Long.parseLong(raw.trim());
            return parsed > 0 ? parsed : defaultRetryAfterSeconds;
        } catch (Exception ignored) {
            return defaultRetryAfterSeconds;
        }
    }

    static boolean rateLimitEnabled() {
        return booleanValue(System.getProperty("wayang.runtime.standalone.execution.rate-limit.enabled", "true"));
    }

    static long rateLimitPerMinute(long defaultRateLimitPerMinute) {
        final String raw = System.getProperty(
                "wayang.runtime.standalone.execution.rate-limit.per-minute",
                String.valueOf(defaultRateLimitPerMinute));
        try {
            final long parsed = Long.parseLong(raw.trim());
            return parsed > 0 ? parsed : defaultRateLimitPerMinute;
        } catch (Exception ignored) {
            return defaultRateLimitPerMinute;
        }
    }

    static int maxInFlightExecutionSubmits(int defaultMaxInFlightExecutionSubmits) {
        final String raw = System.getProperty(
                "wayang.runtime.standalone.execution.max-in-flight-submits",
                String.valueOf(defaultMaxInFlightExecutionSubmits));
        try {
            final int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : defaultMaxInFlightExecutionSubmits;
        } catch (Exception ignored) {
            return defaultMaxInFlightExecutionSubmits;
        }
    }

    static RateLimitDecision consumeRateLimit(
            String tenantId,
            String defaultTenant,
            long defaultRateLimitPerMinute,
            ConcurrentHashMap<String, RateLimitWindow> rateLimitWindows) {
        final long nowMs = System.currentTimeMillis();
        final long nowEpoch = Instant.ofEpochMilli(nowMs).getEpochSecond();
        final long limit = rateLimitPerMinute(defaultRateLimitPerMinute);
        final long windowMillis = 60_000L;
        final String key = optionalStringValue(tenantId) != null ? tenantId : defaultTenant;
        if (!rateLimitEnabled()) {
            return new RateLimitDecision(true, limit, limit, nowEpoch + 60L, 0L);
        }
        final RateLimitWindow window = rateLimitWindows.computeIfAbsent(key, ignored -> new RateLimitWindow());
        synchronized (window) {
            if (window.windowStartMillis == 0L || nowMs - window.windowStartMillis >= windowMillis) {
                window.windowStartMillis = nowMs;
                window.count = 0L;
            }
            final long resetEpoch = Instant.ofEpochMilli(window.windowStartMillis + windowMillis).getEpochSecond();
            if (window.count >= limit) {
                final long retryAfter = Math.max(1L, (window.windowStartMillis + windowMillis - nowMs + 999L) / 1000L);
                return new RateLimitDecision(false, limit, 0L, resetEpoch, retryAfter);
            }
            window.count++;
            final long remaining = Math.max(0L, limit - window.count);
            return new RateLimitDecision(true, limit, remaining, resetEpoch, 0L);
        }
    }

    static boolean acquireInFlightPermit(AtomicInteger inFlightExecutionSubmits, int maxInFlightExecutionSubmits) {
        while (true) {
            int current = inFlightExecutionSubmits.get();
            if (current >= maxInFlightExecutionSubmits) {
                return false;
            }
            if (inFlightExecutionSubmits.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }

    static void releaseInFlightPermit(AtomicInteger inFlightExecutionSubmits) {
        while (true) {
            int current = inFlightExecutionSubmits.get();
            if (current <= 0) {
                return;
            }
            if (inFlightExecutionSubmits.compareAndSet(current, current - 1)) {
                return;
            }
        }
    }

    static final class RateLimitWindow {
        private long windowStartMillis;
        private long count;
    }

    record RateLimitDecision(
            boolean allowed,
            long limit,
            long remaining,
            long resetEpochSeconds,
            long retryAfterSeconds) {
    }

    private static Long parseIfMatchVersion(String ifMatch) {
        final String raw = optionalStringValue(ifMatch);
        if (raw == null || "*".equals(raw)) {
            return null;
        }
        String value = raw.trim();
        if (value.startsWith("W/")) {
            value = value.substring(2).trim();
        }
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String normalizeEtagValue(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        if (value.startsWith("W/")) {
            value = value.substring(2).trim();
        }
        while (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static boolean booleanValue(Object raw) {
        if (raw == null) {
            return false;
        }
        if (raw instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (raw instanceof Number numeric) {
            return numeric.intValue() != 0;
        }
        String value = raw.toString().trim().toLowerCase();
        return "true".equals(value) || "1".equals(value) || "yes".equals(value) || "y".equals(value);
    }

    private static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? null : value;
    }
}
