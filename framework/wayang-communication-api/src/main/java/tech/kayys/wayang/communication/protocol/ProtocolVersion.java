package tech.kayys.wayang.communication.protocol;

public record ProtocolVersion(
        int major,
        int minor
) {

    public ProtocolVersion {
        if (major < 0 || minor < 0) {
            throw new IllegalArgumentException(
                    "Protocol version must be non-negative"
            );
        }
    }

    public static ProtocolVersion of(int major, int minor) {
        return new ProtocolVersion(major, minor);
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }
}
