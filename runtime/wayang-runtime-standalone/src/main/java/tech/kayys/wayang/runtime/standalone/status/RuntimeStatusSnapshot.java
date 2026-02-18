package tech.kayys.wayang.runtime.standalone.status;

import java.util.Map;

/**
 * Full status snapshot for Wayang standalone runtime.
 */
public record RuntimeStatusSnapshot(
        boolean ready,
        Map<String, RuntimeComponentStatus> components) {
}
