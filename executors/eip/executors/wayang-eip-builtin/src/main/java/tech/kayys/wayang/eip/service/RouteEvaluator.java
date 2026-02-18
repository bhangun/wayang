package tech.kayys.wayang.eip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.eip.config.RouterConfig;

/**
 * Real CEL/JsonPath Route Evaluator
 */
@ApplicationScoped
public class RouteEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(RouteEvaluator.class);

    @Inject
    ObjectMapper objectMapper;

    public Uni<List<String>> evaluateRoutes(RouterConfig config, Map<String, Object> context) {
        return Uni.createFrom().item(() -> {
            List<String> results = new ArrayList<>();
            Object message = context.get("message");

            // Sort rules by priority (highest first)
            List<tech.kayys.wayang.eip.config.RouteRule> sortedRules = new ArrayList<>(config.rules());
            sortedRules.sort(java.util.Comparator
                    .comparingInt(tech.kayys.wayang.eip.config.RouteRule::priority).reversed());

            for (tech.kayys.wayang.eip.config.RouteRule rule : sortedRules) {
                if (matches(rule.condition(), message, context)) {
                    results.add(rule.targetNode());
                    if ("first".equalsIgnoreCase(config.strategy())) {
                        break;
                    }
                }
            }

            if (results.isEmpty() && config.defaultRoute() != null) {
                results.add(config.defaultRoute());
            }

            return results;
        });
    }

    public boolean matches(String condition, Object message, Map<String, Object> context) {
        try {
            // Handle different expression types
            if (condition.startsWith("jsonpath:")) {
                return evaluateJsonPath(condition.substring(9), message);
            } else if (condition.startsWith("header.")) {
                return evaluateHeader(condition, context);
            } else if (condition.contains("==") || condition.contains(">") || condition.contains("<")) {
                return evaluateSimpleExpression(condition, message, context);
            } else if ("always".equals(condition)) {
                return true;
            }

            LOG.warn("Unknown condition format: {}, defaulting to false", condition);
            return false;

        } catch (Exception e) {
            LOG.error("Error evaluating condition: {}", condition, e);
            return false;
        }
    }

    private boolean evaluateJsonPath(String path, Object message) {
        try {
            String json = message instanceof String
                    ? (String) message
                    : objectMapper.writeValueAsString(message);

            DocumentContext context = JsonPath.parse(json);
            Object result = context.read(path);

            // Check if result exists and is truthy
            if (result == null)
                return false;
            if (result instanceof Boolean)
                return (Boolean) result;
            if (result instanceof Number)
                return ((Number) result).doubleValue() != 0;
            if (result instanceof String)
                return !((String) result).isEmpty();
            if (result instanceof Collection)
                return !((Collection<?>) result).isEmpty();

            return true;
        } catch (Exception e) {
            LOG.debug("JsonPath evaluation failed: {}", path, e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean evaluateHeader(String condition, Map<String, Object> context) {
        String headerName = condition.substring(7); // Remove "header."
        Map<String, Object> headers = (Map<String, Object>) context.get("headers");
        if (headers == null)
            return false;

        // Check if specific header exists
        if (!headerName.contains("==")) {
            return headers.containsKey(headerName);
        }

        // Check header value
        String[] parts = headerName.split("==");
        if (parts.length != 2)
            return false;

        Object headerValue = headers.get(parts[0].trim());
        String expectedValue = parts[1].trim().replaceAll("'\"", "");

        return headerValue != null && headerValue.toString().equals(expectedValue);
    }

    private boolean evaluateSimpleExpression(String condition, Object message, Map<String, Object> context) {
        try {
            // Handle simple comparisons like "size > 10", "amount >= 100", "status ==
            // 'active'"
            condition = condition.trim();

            String operator;
            String[] parts;

            if (condition.contains(">=")) {
                operator = ">=";
                parts = condition.split(">=");
            } else if (condition.contains("<=")) {
                operator = "<=";
                parts = condition.split("<=");
            } else if (condition.contains("==")) {
                operator = "==";
                parts = condition.split("==");
            } else if (condition.contains("!=")) {
                operator = "!=";
                parts = condition.split("!=");
            } else if (condition.contains(">")) {
                operator = ">";
                parts = condition.split(">");
            } else if (condition.contains("<")) {
                operator = "<";
                parts = condition.split("<");
            } else {
                return false;
            }

            if (parts.length != 2)
                return false;

            String left = parts[0].trim();
            String right = parts[1].trim().replaceAll("['\"]", "");

            // Get left value from message
            Object leftValue = getValueFromMessage(left, message, context);
            if (leftValue == null)
                return false;

            // Compare based on operator
            return compare(leftValue, operator, right);

        } catch (Exception e) {
            LOG.debug("Simple expression evaluation failed: {}", condition, e);
            return false;
        }
    }

    private Object getValueFromMessage(String path, Object message, Map<String, Object> context) {
        // Handle context variables
        if (path.startsWith("context.")) {
            return context.get(path.substring(8));
        }

        // Handle message properties
        if (message instanceof Map) {
            return ((Map<?, ?>) message).get(path);
        }

        // Try JsonPath
        try {
            return evaluateJsonPath("$." + path, message) ? 1 : 0;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean compare(Object left, String operator, String right) {
        try {
            // Numeric comparison
            if (left instanceof Number) {
                double leftNum = ((Number) left).doubleValue();
                double rightNum = Double.parseDouble(right);

                return switch (operator) {
                    case "==" -> leftNum == rightNum;
                    case "!=" -> leftNum != rightNum;
                    case ">" -> leftNum > rightNum;
                    case "<" -> leftNum < rightNum;
                    case ">=" -> leftNum >= rightNum;
                    case "<=" -> leftNum <= rightNum;
                    default -> false;
                };
            }

            // String comparison
            String leftStr = left.toString();
            return switch (operator) {
                case "==" -> leftStr.equals(right);
                case "!=" -> !leftStr.equals(right);
                default -> false;
            };

        } catch (Exception e) {
            return false;
        }
    }
}
