package tech.kayys.wayang.runtime.standalone.status;

/**
 * Component-level status view for the standalone runtime.
 */
public record RuntimeComponentStatus(
        boolean available,
        boolean healthy,
        String detail) {
}
