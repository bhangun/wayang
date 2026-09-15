package tech.kayys.wayang.security.obligation;

/**
 * Standard, well-known obligation types recognized across Wayang execution and security.
 */
public final class StandardObligations {

    public static final ObligationType AUDIT = ObligationType.of("audit");
    public static final ObligationType HITL = ObligationType.of("hitl");
    public static final ObligationType SANDBOX = ObligationType.of("sandbox");
    public static final ObligationType RATE_LIMIT = ObligationType.of("rate-limit");
    public static final ObligationType REDACT = ObligationType.of("redact");
    public static final ObligationType MASK = ObligationType.of("mask");
    public static final ObligationType REQUIRE_APPROVAL = ObligationType.of("require-approval");
    public static final ObligationType REQUIRE_SCOPE = ObligationType.of("require-scope");
    public static final ObligationType REQUIRE_MFA = ObligationType.of("require-mfa");

    private StandardObligations() {}
}
