package tech.kayys.wayang.common.spi;

public class GuardrailResult {
    private final boolean allowed;
    private final String reason;

    private GuardrailResult(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason;
    }

    public boolean isAllowed() { return allowed; }
    public String getReason() { return reason; }

    public static GuardrailResult allow() {
        return new GuardrailResult(true, null);
    }
    
    public static GuardrailResult block(String reason) {
        return new GuardrailResult(false, reason);
    }
}