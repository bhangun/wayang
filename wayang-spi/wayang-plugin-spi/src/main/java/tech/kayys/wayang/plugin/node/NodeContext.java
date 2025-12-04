package tech.kayys.wayang.plugin.node;

import java.util.Map;

import javax.naming.Binding;

import tech.kayys.execution.ExecutionMetadata;
import tech.kayys.node.SecurityContext;
import tech.kayys.wayang.plugin.guardrails.Guardrails;

public interface NodeContext {
    String getRunId();

    String getNodeId();

    String getTenantId();

    String getUserId();

    String getTraceId();

    Map<String, Object> getVariables();

    ExecutionMetadata getMetadata();

    Binding getBinding(String name);

    void setOutput(String name, Object value);

    Object getInput(String name);

    <T> T getInput(String name, Class<T> type);

    ProvenanceContext getProvenance();

    SecurityContext getSecurityContext();

    Guardrails getGuardrails();

    Map<String, Object> getAllInputs();

}
