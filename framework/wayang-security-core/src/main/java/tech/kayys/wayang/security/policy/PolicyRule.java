package tech.kayys.wayang.security.policy;

import tech.kayys.wayang.security.obligation.Obligation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A single policy rule: subject pattern + capability pattern + action + effect + obligations.
 * Uses glob-style wildcards: {@code *} matches any single segment, {@code **} matches anything.
 */
public record PolicyRule(
        String id,
        String subjectPattern,
        String capabilityPattern,
        String actionPattern,
        PolicyEffect effect,
        List<Obligation> obligations,
        int priority
) {

    public PolicyRule {
        Objects.requireNonNull(id,                "id");
        Objects.requireNonNull(effect,            "effect");
        subjectPattern    = subjectPattern    == null ? "*" : subjectPattern;
        capabilityPattern = capabilityPattern == null ? "*" : capabilityPattern;
        actionPattern     = actionPattern     == null ? "*" : actionPattern;
        obligations       = obligations       == null ? List.of() : List.copyOf(obligations);
    }

    /** Returns true if this rule matches the given request fields. */
    public boolean matches(String subjectId, String capability, String action) {
        return glob(subjectPattern,    subjectId  == null ? "" : subjectId)
            && glob(capabilityPattern, capability == null ? "" : capability)
            && glob(actionPattern,     action     == null ? "" : action);
    }

    private static boolean glob(String pattern, String value) {
        if ("*".equals(pattern) || "**".equals(pattern)) return true;
        if (pattern.equals(value)) return true;
        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*') {
                if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                    sb.append(".*");
                    i++;
                } else {
                    sb.append(".*");
                }
            } else if (c == '.') {
                sb.append("\\.");
            } else if (c == '?') {
                sb.append(".");
            } else if ("()[]{}+^$|\\".indexOf(c) != -1) {
                sb.append("\\").append(c);
            } else {
                sb.append(c);
            }
        }
        sb.append("$");
        return value.matches(sb.toString());
    }

    public static PolicyRule allow(String id, String subjectPattern, String capabilityPattern) {
        return new PolicyRule(id, subjectPattern, capabilityPattern, "*", PolicyEffect.ALLOW, List.of(), 0);
    }

    public static PolicyRule deny(String id, String subjectPattern, String capabilityPattern) {
        return new PolicyRule(id, subjectPattern, capabilityPattern, "*", PolicyEffect.DENY, List.of(), 0);
    }
}
