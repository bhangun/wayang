package tech.kayys.wayang.tool.nono;

public enum NonoNetworkMode {
    BLOCKED(0),
    ALLOW_ALL(1),
    PROXY_ONLY(2);

    private final int value;

    NonoNetworkMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static NonoNetworkMode fromValue(int value) {
        for (NonoNetworkMode mode : values()) {
            if (mode.value == value) return mode;
        }
        return BLOCKED;
    }
}
