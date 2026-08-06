package tech.kayys.wayang.sdk.client;

import tech.kayys.wayang.sdk.agent.WayangAgent;
import tech.kayys.wayang.sdk.agent.WayangAgentListener;

/**
 * API for invoking Wayang inference using WayangAgent.
 */
public final class WayangInferenceApi {

    private final WayangAgent agent;

    public WayangInferenceApi(WayangAgent agent) {
        this.agent = agent;
    }

    public void send(String message, WayangAgentListener listener) {
        agent.send(message, listener);
    }
    
    public WayangAgent getAgent() {
        return agent;
    }
}
