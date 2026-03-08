package tech.kayys.wayang.runtime.standalone.resource;

import java.util.Set;

final class ProjectsExecutionStatusSupport {
    private ProjectsExecutionStatusSupport() {
    }

    static String normalizeStatus(Object rawStatus, String statusUnknown) {
        if (rawStatus == null) {
            return statusUnknown;
        }
        final String status = rawStatus.toString().trim();
        if (status.isEmpty()) {
            return statusUnknown;
        }
        return status.toUpperCase().replace('-', '_').replace(' ', '_');
    }

    static boolean isStatusTransitionAllowed(String fromRaw, String toRaw, String statusUnknown) {
        final String from = normalizeStatus(fromRaw, statusUnknown);
        final String to = normalizeStatus(toRaw, statusUnknown);

        if (statusUnknown.equals(from) || statusUnknown.equals(to)) {
            return true;
        }
        if (from.equals(to)) {
            return true;
        }
        if (isTerminalStatus(from)) {
            return false;
        }

        return switch (from) {
            case "QUEUED" -> Set.of("STARTED", "RUNNING", "FAILED", "STOPPED").contains(to);
            case "STARTED" -> Set.of("RUNNING", "COMPLETED", "FAILED", "STOPPED", "PAUSED",
                    "WAITING_FOR_HUMAN_INPUT").contains(to);
            case "RUNNING" -> Set.of("COMPLETED", "FAILED", "STOPPED", "PAUSED",
                    "WAITING_FOR_HUMAN_INPUT").contains(to);
            case "PAUSED", "WAITING_FOR_HUMAN_INPUT" -> Set.of("RUNNING", "FAILED", "STOPPED").contains(to);
            default -> true;
        };
    }

    private static boolean isTerminalStatus(String status) {
        return "COMPLETED".equals(status) || "FAILED".equals(status) || "STOPPED".equals(status);
    }
}
