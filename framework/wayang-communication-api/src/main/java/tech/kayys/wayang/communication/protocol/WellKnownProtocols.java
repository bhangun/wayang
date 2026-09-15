package tech.kayys.wayang.communication.protocol;

public final class WellKnownProtocols {

    private WellKnownProtocols() {
    }

    public static final ProtocolId IN_PROCESS =
            new ProtocolId("wayang.inprocess");

    public static final ProtocolId A2A =
            new ProtocolId("a2a");

    public static final ProtocolId ANP =
            new ProtocolId("anp");
}
