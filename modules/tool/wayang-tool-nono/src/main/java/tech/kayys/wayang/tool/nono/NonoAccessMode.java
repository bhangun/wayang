package tech.kayys.wayang.tool.nono;

public enum NonoAccessMode {
    READ(0),
    WRITE(1),
    READ_WRITE(2);

    private final int value;

    NonoAccessMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
