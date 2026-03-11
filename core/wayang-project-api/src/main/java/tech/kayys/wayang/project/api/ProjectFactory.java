package tech.kayys.wayang.project.api;

import java.util.Arrays;
import java.util.List;

/**
 * Factory for creating project descriptors based on intent.
 */
public class ProjectFactory {
    
    /**
     * Common capabilities available in Wayang platform.
     */
    public static final List<String> AVAILABLE_CAPABILITIES = Arrays.asList(
        "agent", "rag", "web-search", "tool-execution", "guardrails", 
        "memory", "workflow", "multi-agent", "hitl", "embedding"
    );
    
    /**
     * Create a project descriptor from high-level intent.
     */
    public static ProjectDescriptor fromIntent(String intent) {
        ProjectDescriptor descriptor = new ProjectDescriptor();
        descriptor.setIntent(intent);
        
        String normalizedIntent = intent.toLowerCase();
        
        // Auto-detect capabilities from intent
        if (normalizedIntent.contains("rag") || normalizedIntent.contains("retrieval") || 
            normalizedIntent.contains("knowledge") || normalizedIntent.contains("document")) {
            descriptor.getCapabilities().add("rag");
        }
        if (normalizedIntent.contains("web") || normalizedIntent.contains("search") || 
            normalizedIntent.contains("internet") || normalizedIntent.contains("online")) {
            descriptor.getCapabilities().add("web-search");
        }
        if (normalizedIntent.contains("agent") || normalizedIntent.contains("chat") || 
            normalizedIntent.contains("assistant") || normalizedIntent.contains("bot")) {
            descriptor.getCapabilities().add("agent");
        }
        if (normalizedIntent.contains("tool") || normalizedIntent.contains("function") || 
            normalizedIntent.contains("api") || normalizedIntent.contains("integration")) {
            descriptor.getCapabilities().add("tool-execution");
        }
        if (normalizedIntent.contains("guard") || normalizedIntent.contains("safety") || 
            normalizedIntent.contains("filter") || normalizedIntent.contains("moderate")) {
            descriptor.getCapabilities().add("guardrails");
        }
        if (normalizedIntent.contains("memory") || normalizedIntent.contains("history") || 
            normalizedIntent.contains("conversation") || normalizedIntent.contains("context")) {
            descriptor.getCapabilities().add("memory");
        }
        if (normalizedIntent.contains("workflow") || normalizedIntent.contains("pipeline") || 
            normalizedIntent.contains("orchestrate") || normalizedIntent.contains("flow")) {
            descriptor.getCapabilities().add("workflow");
        }
        if (normalizedIntent.contains("multi") || normalizedIntent.contains("collaborate") || 
            normalizedIntent.contains("coordinate")) {
            descriptor.getCapabilities().add("multi-agent");
        }
        
        // Default to agent if no capabilities detected
        if (descriptor.getCapabilities().isEmpty()) {
            descriptor.getCapabilities().add("agent");
        }
        
        return descriptor;
    }
    
    /**
     * Create a simple agent project.
     */
    public static ProjectDescriptor createAgentProject(String name, String description) {
        ProjectDescriptor descriptor = new ProjectDescriptor();
        descriptor.setName(name);
        descriptor.setDescription(description);
        descriptor.setArtifactId(name.toLowerCase().replaceAll("\\s+", "-"));
        descriptor.getCapabilities().add("agent");
        
        WorkflowDescriptor workflow = new WorkflowDescriptor();
        workflow.setName("main-workflow");
        workflow.setDescription("Main agent workflow");
        workflow.setType("agent-workflow");
        
        NodeDescriptor agentNode = new NodeDescriptor();
        agentNode.setId("agent-1");
        agentNode.setType("agent-task");
        agentNode.setExecutor("agent");
        agentNode.setDescription("Main agent node");
        agentNode.getConfig().put("skillId", "common");
        
        workflow.getNodes().add(agentNode);
        descriptor.getWorkflows().add(workflow);
        
        return descriptor;
    }
    
    /**
     * Create a RAG-enabled project.
     */
    public static ProjectDescriptor createRagProject(String name, String description) {
        ProjectDescriptor descriptor = createAgentProject(name, description);
        descriptor.getCapabilities().add("rag");
        
        WorkflowDescriptor workflow = descriptor.getWorkflows().get(0);
        
        NodeDescriptor ragNode = new NodeDescriptor();
        ragNode.setId("rag-1");
        ragNode.setType("rag-task");
        ragNode.setExecutor("rag");
        ragNode.setDescription("RAG retrieval node");
        ragNode.getConfig().put("topK", 5);
        ragNode.getConfig().put("rerank", true);
        
        workflow.getNodes().add(0, ragNode);
        
        ConnectionDescriptor conn = new ConnectionDescriptor("rag-1", "agent-1");
        workflow.getConnections().add(conn);
        
        return descriptor;
    }
    
    /**
     * Create a web-search enabled project.
     */
    public static ProjectDescriptor createWebSearchProject(String name, String description) {
        ProjectDescriptor descriptor = createAgentProject(name, description);
        descriptor.getCapabilities().add("web-search");
        
        WorkflowDescriptor workflow = descriptor.getWorkflows().get(0);
        
        NodeDescriptor searchNode = new NodeDescriptor();
        searchNode.setId("web-search-1");
        searchNode.setType("web-search-task");
        searchNode.setExecutor("web-search");
        searchNode.setDescription("Web search node");
        searchNode.getConfig().put("provider", "duckduckgo");
        searchNode.getConfig().put("maxResults", 5);
        
        workflow.getNodes().add(0, searchNode);
        
        ConnectionDescriptor conn = new ConnectionDescriptor("web-search-1", "agent-1");
        workflow.getConnections().add(conn);
        
        return descriptor;
    }
}
