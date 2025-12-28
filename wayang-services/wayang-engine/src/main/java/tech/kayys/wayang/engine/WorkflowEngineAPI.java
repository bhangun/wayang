package tech.kayys.wayang.engine;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.sdk.dto.WorkflowRunResponse;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Fluent API for Wayang Workflow Engine.
 * Provides an intuitive, type-safe way to execute workflows.
 * 
 * Architecture:
 * - Supports all three workflow types: agentic, integration, business
 * - Enforces engine sovereignty (all operations go through engine)
 * - Provides error handling and retry semantics
 * - Multi-tenant isolation enforced
 * - Audit and provenance built-in
 * 
 * Usage Examples:
 * 
 * 1. Agentic Workflow:
 * 
 * <pre>
 * WorkflowRunResponse result = workflowAPI
 *         .agentic("customer-support-bot")
 *         .forTenant("acme-corp")
 *         .withAgent("support-agent-v2")
 *         .enableTools("knowledge-base", "ticket-system")
 *         .withInput("query", "How do I reset my password?")
 *         .execute()
 *         .await().indefinitely();
 * </pre>
 * 
 * 2. Integration Workflow:
 * 
 * <pre>
 * WorkflowRunResponse result = workflowAPI
 *         .integration("salesforce-to-erp")
 *         .forTenant("acme-corp")
 *         .fromConnector("salesforce-api")
 *         .toConnector("sap-erp")
 *         .withTransformation("mapping-v2.yaml")
 *         .batchSize(1000)
 *         .execute()
 *         .await().indefinitely();
 * </pre>
 * 
 * 3. Business Workflow:
 * 
 * <pre>
 * WorkflowRunResponse result = workflowAPI
 *         .business("expense-approval")
 *         .forTenant("acme-corp")
 *         .triggeredBy("user:john.doe")
 *         .withApprovalChain("manager", "finance", "cfo")
 *         .sla(48) // hours
 *         .withInput("amount", 1500.00)
 *         .execute()
 *         .await().indefinitely();
 * </pre>
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class WorkflowEngineAPI {

    @Inject
    @RestClient
    WorkflowEngineClient engineClient;

    /**
     * Start building an agentic workflow execution.
     */
    public AgenticWorkflowBuilder agentic(String workflowId) {
        return new AgenticWorkflowBuilder(engineClient, workflowId);
    }

    /**
     * Start building an integration workflow execution.
     */
    public IntegrationWorkflowBuilder integration(String workflowId) {
        return new IntegrationWorkflowBuilder(engineClient, workflowId);
    }

    /**
     * Start building a business automation workflow execution.
     */
    public BusinessWorkflowBuilder business(String workflowId) {
        return new BusinessWorkflowBuilder(engineClient, workflowId);
    }

    /**
     * Start building a unified workflow execution (auto-detect type).
     */
    public UnifiedWorkflowBuilder workflow(String workflowId) {
        return new UnifiedWorkflowBuilder(engineClient, workflowId);
    }

    // ========================================================================
    // AGENTIC WORKFLOW BUILDER
    // ========================================================================

    public static class AgenticWorkflowBuilder {
        private final WorkflowEngineClient client;
        private final AgenticWorkflowRequest.Builder requestBuilder;

        AgenticWorkflowBuilder(WorkflowEngineClient client, String workflowId) {
            this.client = client;
            this.requestBuilder = AgenticWorkflowRequest.builder()
                    .workflowId(workflowId);
        }

        public AgenticWorkflowBuilder version(String version) {
            requestBuilder.workflowVersion(version);
            return this;
        }

        public AgenticWorkflowBuilder forTenant(String tenantId) {
            requestBuilder.tenantId(tenantId);
            return this;
        }

        public AgenticWorkflowBuilder triggeredBy(String triggeredBy) {
            requestBuilder.triggeredBy(triggeredBy);
            return this;
        }

        public AgenticWorkflowBuilder withAgent(String agentId) {
            AgentConfig config = AgentConfig.builder()
                    .primaryAgent(agentId)
                    .build();
            requestBuilder.agentConfig(config);
            return this;
        }

        public AgenticWorkflowBuilder enableTools(String... tools) {
            AgentConfig config = requestBuilder.build().getAgentConfig();
            if (config == null) {
                config = AgentConfig.builder()
                        .toolsEnabled(List.of(tools))
                        .build();
            } else {
                config.setToolsEnabled(List.of(tools));
            }
            requestBuilder.agentConfig(config);
            return this;
        }

        public AgenticWorkflowBuilder withOrchestration(OrchestrationStrategy strategy) {
            AgentConfig config = requestBuilder.build().getAgentConfig();
            if (config == null) {
                config = AgentConfig.builder()
                        .orchestrationStrategy(strategy)
                        .build();
            } else {
                config.setOrchestrationStrategy(strategy);
            }
            requestBuilder.agentConfig(config);
            return this;
        }

        public AgenticWorkflowBuilder maxIterations(int max) {
            AgentConfig config = requestBuilder.build().getAgentConfig();
            if (config == null) {
                config = AgentConfig.builder()
                        .maxIterations(max)
                        .build();
            } else {
                config.setMaxIterations(max);
            }
            requestBuilder.agentConfig(config);
            return this;
        }

        public AgenticWorkflowBuilder enableRAG() {
            AgentConfig config = requestBuilder.build().getAgentConfig();
            if (config == null) {
                config = AgentConfig.builder()
                        .ragEnabled(true)
                        .build();
            } else {
                config.setRagEnabled(true);
            }
            requestBuilder.agentConfig(config);
            return this;
        }

        public AgenticWorkflowBuilder withMemory(String namespace) {
            AgentConfig config = requestBuilder.build().getAgentConfig();
            if (config == null) {
                config = AgentConfig.builder()
                        .memoryNamespace(namespace)
                        .build();
            } else {
                config.setMemoryNamespace(namespace);
            }
            requestBuilder.agentConfig(config);
            return this;
        }

        public AgenticWorkflowBuilder streaming() {
            requestBuilder.streamingEnabled(true);
            return this;
        }

        public AgenticWorkflowBuilder confidenceThreshold(double threshold) {
            requestBuilder.confidenceThreshold(threshold);
            return this;
        }

        public AgenticWorkflowBuilder withInput(String key, Object value) {
            requestBuilder.input(key, value);
            return this;
        }

        public AgenticWorkflowBuilder withInputs(Map<String, Object> inputs) {
            inputs.forEach(requestBuilder::input);
            return this;
        }

        public AgenticWorkflowBuilder metadata(String key, Object value) {
            requestBuilder.metadata(key, value);
            return this;
        }

        /**
         * Execute workflow asynchronously.
         */
        public Uni<WorkflowRunResponse> execute() {
            return client.executeAgenticWorkflow(requestBuilder.build());
        }

        /**
         * Execute workflow with streaming output.
         */
        public Multi<AgentExecutionEvent> executeStream() {
            return client.executeAgenticWorkflowStream(requestBuilder.build());
        }

        /**
         * Simulate workflow execution without side effects.
         */
        public Uni<SimulationResponse> simulate() {
            UnifiedWorkflowRequest unifiedRequest = new UnifiedWorkflowRequest();
            AgenticWorkflowRequest agenticRequest = requestBuilder.build();
            unifiedRequest.setWorkflowId(agenticRequest.getWorkflowId());
            unifiedRequest.setAgentConfig(agenticRequest.getAgentConfig());
            unifiedRequest.setInputs(agenticRequest.getInputs());
            return client.simulateWorkflow(unifiedRequest);
        }
    }

    // ========================================================================
    // INTEGRATION WORKFLOW BUILDER
    // ========================================================================

    public static class IntegrationWorkflowBuilder {
        private final WorkflowEngineClient client;
        private final IntegrationWorkflowRequest.Builder requestBuilder;
        private final IntegrationConfig.Builder configBuilder;
        private final BatchConfig.Builder batchBuilder;

        IntegrationWorkflowBuilder(WorkflowEngineClient client, String workflowId) {
            this.client = client;
            this.requestBuilder = IntegrationWorkflowRequest.builder()
                    .workflowId(workflowId);
            this.configBuilder = IntegrationConfig.builder();
            this.batchBuilder = BatchConfig.builder();
        }

        public IntegrationWorkflowBuilder version(String version) {
            // Note: requestBuilder doesn't have version method, would need to add
            return this;
        }

        public IntegrationWorkflowBuilder forTenant(String tenantId) {
            requestBuilder.tenantId(tenantId);
            return this;
        }

        public IntegrationWorkflowBuilder triggeredBy(String triggeredBy) {
            requestBuilder.triggeredBy(triggeredBy);
            return this;
        }

        public IntegrationWorkflowBuilder fromConnector(String connector) {
            configBuilder.sourceConnector(connector);
            return this;
        }

        public IntegrationWorkflowBuilder toConnector(String connector) {
            configBuilder.targetConnector(connector);
            return this;
        }

        public IntegrationWorkflowBuilder withTransformation(String rules) {
            configBuilder.transformationRules(rules);
            return this;
        }

        public IntegrationWorkflowBuilder errorStrategy(ErrorStrategy strategy) {
            configBuilder.errorStrategy(strategy);
            return this;
        }

        public IntegrationWorkflowBuilder idempotencyKey(String key) {
            configBuilder.idempotencyKey(key);
            return this;
        }

        public IntegrationWorkflowBuilder transactional() {
            requestBuilder.transactional(true);
            return this;
        }

        public IntegrationWorkflowBuilder rateLimit(long perSecond) {
            requestBuilder.rateLimit(perSecond);
            return this;
        }

        public IntegrationWorkflowBuilder batchSize(int size) {
            batchBuilder.enabled(true).batchSize(size);
            return this;
        }

        public IntegrationWorkflowBuilder parallelism(int parallelism) {
            batchBuilder.parallelism(parallelism);
            return this;
        }

        public IntegrationWorkflowBuilder batchTimeout(long ms) {
            batchBuilder.batchTimeout(ms);
            return this;
        }

        public IntegrationWorkflowBuilder withInput(String key, Object value) {
            requestBuilder.input(key, value);
            return this;
        }

        public IntegrationWorkflowBuilder withInputs(Map<String, Object> inputs) {
            inputs.forEach(requestBuilder::input);
            return this;
        }

        public IntegrationWorkflowBuilder connectorParam(String key, Object value) {
            configBuilder.connectorParam(key, value);
            return this;
        }

        /**
         * Execute integration workflow.
         */
        public Uni<WorkflowRunResponse> execute() {
            requestBuilder.integrationConfig(configBuilder.build());
            requestBuilder.batchConfig(batchBuilder.build());
            return client.executeIntegrationWorkflow(requestBuilder.build());
        }

        /**
         * Execute integration workflow with batch processing.
         */
        public Uni<BatchWorkflowRunResponse> executeBatch() {
            requestBuilder.integrationConfig(configBuilder.build());
            batchBuilder.enabled(true);
            requestBuilder.batchConfig(batchBuilder.build());
            return client.executeIntegrationWorkflowBatch(requestBuilder.build());
        }

        /**
         * Simulate integration workflow.
         */
        public Uni<SimulationResponse> simulate() {
            UnifiedWorkflowRequest unifiedRequest = new UnifiedWorkflowRequest();
            IntegrationWorkflowRequest integrationRequest = requestBuilder.build();
            unifiedRequest.setWorkflowId(integrationRequest.getWorkflowId());
            unifiedRequest.setIntegrationConfig(integrationRequest.getIntegrationConfig());
            unifiedRequest.setInputs(integrationRequest.getInputs());
            return client.simulateWorkflow(unifiedRequest);
        }
    }

    // ========================================================================
    // BUSINESS WORKFLOW BUILDER
    // ========================================================================

    public static class BusinessWorkflowBuilder {
        private final WorkflowEngineClient client;
        private final BusinessWorkflowRequest.Builder requestBuilder;
        private final BusinessConfig.Builder configBuilder;

        BusinessWorkflowBuilder(WorkflowEngineClient client, String workflowId) {
            this.client = client;
            this.requestBuilder = BusinessWorkflowRequest.builder()
                    .workflowId(workflowId);
            this.configBuilder = BusinessConfig.builder();
        }

        public BusinessWorkflowBuilder version(String version) {
            // Note: requestBuilder doesn't have version method
            return this;
        }

        public BusinessWorkflowBuilder forTenant(String tenantId) {
            requestBuilder.tenantId(tenantId);
            return this;
        }

        public BusinessWorkflowBuilder triggeredBy(String triggeredBy) {
            requestBuilder.triggeredBy(triggeredBy);
            return this;
        }

        public BusinessWorkflowBuilder withApprovalChain(String... approvers) {
            configBuilder.approvalChain(List.of(approvers));
            return this;
        }

        public BusinessWorkflowBuilder sla(int hours) {
            configBuilder.slaHours(hours);
            return this;
        }

        public BusinessWorkflowBuilder escalationPolicy(EscalationPolicy policy) {
            configBuilder.escalationPolicy(policy);
            return this;
        }

        public BusinessWorkflowBuilder withForm(String formTemplate) {
            configBuilder.formTemplate(formTemplate);
            return this;
        }

        public BusinessWorkflowBuilder notifyVia(String... channels) {
            configBuilder.notificationChannels(List.of(channels));
            return this;
        }

        public BusinessWorkflowBuilder businessRule(String key, Object value) {
            configBuilder.businessRule(key, value);
            return this;
        }

        public BusinessWorkflowBuilder priority(String priority) {
            requestBuilder.priority(priority);
            return this;
        }

        public BusinessWorkflowBuilder requiresApproval(boolean required) {
            requestBuilder.requiresApproval(required);
            return this;
        }

        public BusinessWorkflowBuilder withInput(String key, Object value) {
            requestBuilder.input(key, value);
            return this;
        }

        public BusinessWorkflowBuilder withInputs(Map<String, Object> inputs) {
            inputs.forEach(requestBuilder::input);
            return this;
        }

        /**
         * Execute business workflow.
         */
        public Uni<WorkflowRunResponse> execute() {
            requestBuilder.businessConfig(configBuilder.build());
            return client.executeBusinessWorkflow(requestBuilder.build());
        }

        /**
         * Simulate business workflow.
         */
        public Uni<SimulationResponse> simulate() {
            UnifiedWorkflowRequest unifiedRequest = new UnifiedWorkflowRequest();
            BusinessWorkflowRequest businessRequest = requestBuilder.build();
            unifiedRequest.setWorkflowId(businessRequest.getWorkflowId());
            unifiedRequest.setBusinessConfig(businessRequest.getBusinessConfig());
            unifiedRequest.setInputs(businessRequest.getInputs());
            return client.simulateWorkflow(unifiedRequest);
        }
    }

    // ========================================================================
    // UNIFIED WORKFLOW BUILDER
    // ========================================================================

    public static class UnifiedWorkflowBuilder {
        private final WorkflowEngineClient client;
        private final UnifiedWorkflowRequest.Builder requestBuilder;

        UnifiedWorkflowBuilder(WorkflowEngineClient client, String workflowId) {
            this.client = client;
            this.requestBuilder = UnifiedWorkflowRequest.builder()
                    .workflowId(workflowId);
        }

        public UnifiedWorkflowBuilder forTenant(String tenantId) {
            requestBuilder.tenantId(tenantId);
            return this;
        }

        public UnifiedWorkflowBuilder withAgentConfig(AgentConfig config) {
            requestBuilder.agentConfig(config);
            return this;
        }

        public UnifiedWorkflowBuilder withIntegrationConfig(IntegrationConfig config) {
            requestBuilder.integrationConfig(config);
            return this;
        }

        public UnifiedWorkflowBuilder withBusinessConfig(BusinessConfig config) {
            requestBuilder.businessConfig(config);
            return this;
        }

        public UnifiedWorkflowBuilder withInput(String key, Object value) {
            requestBuilder.input(key, value);
            return this;
        }

        public Uni<WorkflowRunResponse> execute() {
            return client.executeWorkflow(requestBuilder.build());
        }

        public Uni<WorkflowRunResponse> executeSync(long timeoutMs) {
            return client.executeWorkflowSync(requestBuilder.build(), timeoutMs);
        }

        public Uni<SimulationResponse> simulate() {
            return client.simulateWorkflow(requestBuilder.build());
        }
    }
}