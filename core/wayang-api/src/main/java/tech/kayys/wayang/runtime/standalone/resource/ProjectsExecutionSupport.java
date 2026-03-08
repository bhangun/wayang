package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.enterprise.inject.Instance;
import tech.kayys.wayang.security.secrets.core.SecretManager;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class ProjectsExecutionSupport {
    private ProjectsExecutionSupport() {
    }

    static Map<String, Object> findProjectById(String projectId) throws IOException {
        if (projectId == null || projectId.isBlank()) {
            return null;
        }
        return ProjectsFileStore.readProjects().stream()
                .filter(p -> projectId.equals(String.valueOf(p.get("projectId"))))
                .findFirst()
                .orElse(null);
    }

    static boolean isStandalonePersistenceUnavailable(Throwable failure) {
        String message = failure != null ? String.valueOf(failure.getMessage()) : "";
        return message.contains("Mutiny.SessionFactory bean not found")
                || message.contains("Mutiny$SessionFactory")
                || message.contains("quarkus.hibernate-orm.enabled=false");
    }

    static Map<String, Object> buildExecutionFailurePayload(Throwable failure) {
        final String fullMessage = flattenThrowableMessage(failure);
        if (looksLikeInvalidWorkflowDefinition(fullMessage)) {
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("error", "Failed to execute project spec");
            payload.put("errorCode", "EXECUTION_WORKFLOW_INVALID");
            payload.put("message", "Invalid workflow definition");
            payload.put("details", Map.of(
                    "phase", "workflow-definition-validation",
                    "suggestion", "Use dryRun/validate-callable/preview-output-bindings to validate child contracts before execution"));
            return payload;
        }
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", "Failed to execute project spec");
        payload.put("errorCode", "EXECUTION_INTERNAL_ERROR");
        payload.put("message", firstNonBlank(
                optionalStringValue(failure != null ? failure.getMessage() : null),
                failure != null ? failure.getClass().getSimpleName() : "Unknown failure"));
        return payload;
    }

    static Map<String, Object> pruneNulls(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return source.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> pruneValue(entry.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new));
    }

    static SecretManager resolveSecretManager(Instance<SecretManager> secretManagerInstance) {
        if (secretManagerInstance == null) {
            return null;
        }
        try {
            for (SecretManager manager : secretManagerInstance) {
                if (manager != null) {
                    return manager;
                }
            }
        } catch (Exception ignored) {
            // no-op; caller will treat as unavailable
        }
        return null;
    }

    static Map<String, Object> buildExecutionContext(
            String projectId,
            String requestId,
            Map<String, Object> requestBody) {
        final Map<String, Object> body = requestBody != null ? requestBody : Map.of();
        final String parentExecutionId = optionalStringValue(body.get("parentExecutionId"));
        final String parentProjectId = optionalStringValue(body.get("parentProjectId"));
        final String parentNodeId = optionalStringValue(body.get("parentNodeId"));
        final String relationType = optionalStringValue(body.get("relationType"));
        final String requestedCorrelationId = optionalStringValue(body.get("correlationId"));
        final boolean hasExplicitContext = parentExecutionId != null
                || parentProjectId != null
                || parentNodeId != null
                || relationType != null
                || requestedCorrelationId != null;

        if (!hasExplicitContext) {
            return Map.of();
        }

        final String correlationId = firstNonBlank(requestedCorrelationId, requestId);
        final Map<String, Object> context = new LinkedHashMap<>();
        context.put("relationType", relationType != null ? relationType : "subworkflow");
        if (parentExecutionId != null) {
            context.put("parentExecutionId", parentExecutionId);
        }
        if (parentProjectId != null) {
            context.put("parentProjectId", parentProjectId);
        }
        if (parentNodeId != null) {
            context.put("parentNodeId", parentNodeId);
        }
        context.put("childProjectId", projectId);
        if (correlationId != null) {
            context.put("correlationId", correlationId);
        }
        return context;
    }

    private static String flattenThrowableMessage(Throwable failure) {
        if (failure == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        Throwable cursor = failure;
        int guard = 0;
        while (cursor != null && guard < 8) {
            if (!builder.isEmpty()) {
                builder.append(" | ");
            }
            builder.append(firstNonBlank(optionalStringValue(cursor.getMessage()), cursor.getClass().getSimpleName()));
            cursor = cursor.getCause();
            guard++;
        }
        return builder.toString();
    }

    private static boolean looksLikeInvalidWorkflowDefinition(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        final String normalized = message.toLowerCase();
        return normalized.contains("invalid workflow definition")
                || normalized.contains("workflow definition is invalid")
                || normalized.contains("workflow validation failed");
    }

    private static Object pruneValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> rawMap) {
            final Map<String, Object> cast = new LinkedHashMap<>();
            rawMap.forEach((k, v) -> {
                if (k != null) {
                    cast.put(String.valueOf(k), v);
                }
            });
            return pruneNulls(cast);
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(ProjectsExecutionSupport::pruneValue)
                    .filter(item -> item != null)
                    .toList();
        }
        return value;
    }

    private static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? null : value;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
