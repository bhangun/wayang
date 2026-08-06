package tech.kayys.wayang.harness.spi;
import tech.kayys.wayang.agent.AgentRequest;
public interface HarnessRequest {
    AgentRequest getAgentRequest();
    HarnessConfig getConfig();
}
