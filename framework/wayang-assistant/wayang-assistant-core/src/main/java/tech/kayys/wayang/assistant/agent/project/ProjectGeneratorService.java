package tech.kayys.wayang.assistant.agent.project;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import tech.kayys.wayang.project.api.ConnectionDescriptor;
import tech.kayys.wayang.project.api.NodeDescriptor;
import tech.kayys.wayang.project.api.ProjectDescriptor;
import tech.kayys.wayang.project.api.ProjectFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for generating Wayang projects from natural language intent.
 */
@ApplicationScoped
public class ProjectGeneratorService {

    private static final Logger LOG = Logger.getLogger(ProjectGeneratorService.class);

    public ProjectDescriptor generateProject(String intent) {
        LOG.infof("Generating project from intent: %s", intent);

        ProjectDescriptor descriptor = ProjectFactory.fromIntent(intent);

        if (descriptor.getName() == null) {
            descriptor.setName(generateNameFromIntent(intent));
            descriptor.setArtifactId(descriptor.getName().toLowerCase().replaceAll("\\s+", "-"));
        }
        if (descriptor.getDescription() == null) {
            descriptor.setDescription("Auto-generated Wayang project: " + intent);
        }

        Set<String> caps = detectCapabilities(intent);
        descriptor.getCapabilities().addAll(caps);

        if (caps.contains("rag") && caps.contains("web-search")) {
            descriptor = ProjectFactory.createRagProject(descriptor.getName(), descriptor.getDescription());
            addWebSearchNode(descriptor);
        } else if (caps.contains("rag")) {
            descriptor = ProjectFactory.createRagProject(descriptor.getName(), descriptor.getDescription());
        } else if (caps.contains("web-search")) {
            descriptor = ProjectFactory.createWebSearchProject(descriptor.getName(), descriptor.getDescription());
        } else if (caps.contains("orchestrator") || caps.contains("multi-agent")) {
            descriptor = ProjectFactory.createAgentProject(descriptor.getName(), descriptor.getDescription());
            addOrchestratorNode(descriptor);
        } else if (caps.contains("hitl")) {
            descriptor = ProjectFactory.createAgentProject(descriptor.getName(), descriptor.getDescription());
            addHitlNode(descriptor);
        } else {
            descriptor = ProjectFactory.createAgentProject(descriptor.getName(), descriptor.getDescription());
        }

        descriptor.getCapabilities().addAll(caps);
        LOG.infof("Generated project: %s caps=%s", descriptor.getName(), descriptor.getCapabilities());
        return descriptor;
    }

    private Set<String> detectCapabilities(String intent) {
        String lower = intent.toLowerCase();
        Set<String> caps = new LinkedHashSet<>();
        if (lower.contains("rag") || lower.contains("retrieval") || lower.contains("knowledge base")
                || lower.contains("document") || lower.contains("vector")) caps.add("rag");
        if (lower.contains("web search") || lower.contains("websearch") || lower.contains("internet")
                || lower.contains("browse") || lower.contains("search the web")) caps.add("web-search");
        if (lower.contains("multi-agent") || lower.contains("multiple agents")
                || lower.contains("orchestrat")) caps.add("orchestrator");
        if (lower.contains("hitl") || lower.contains("human in the loop")
                || lower.contains("approval") || lower.contains("review")) caps.add("hitl");
        if (lower.contains("guardrail") || lower.contains("safety") || lower.contains("filter")) caps.add("guardrails");
        if (caps.isEmpty()) caps.add("agent");
        return caps;
    }

    private String generateNameFromIntent(String intent) {
        String[] words = intent.split("\\s+");
        if (words.length >= 2) {
            return Arrays.stream(words).limit(3)
                    .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
        }
        return "Wayang " + intent.substring(0, Math.min(20, intent.length()));
    }

    private void addWebSearchNode(ProjectDescriptor descriptor) {
        if (descriptor.getWorkflows().isEmpty()) return;
        var workflow = descriptor.getWorkflows().get(0);
        var node = new NodeDescriptor();
        node.setId("web-search-1");
        node.setType("web-search-task");
        node.setExecutor("web-search");
        node.setDescription("Web search for real-time information");
        node.getConfig().put("provider", "duckduckgo");
        node.getConfig().put("maxResults", 5);
        workflow.getNodes().add(0, node);
        workflow.getConnections().add(new ConnectionDescriptor("web-search-1", "agent-1"));
    }

    private void addOrchestratorNode(ProjectDescriptor descriptor) {
        if (descriptor.getWorkflows().isEmpty()) return;
        var workflow = descriptor.getWorkflows().get(0);
        var node = new NodeDescriptor();
        node.setId("orchestrator-1");
        node.setType("orchestrator-agent");
        node.setExecutor("orchestrator");
        node.setDescription("Orchestrator that coordinates sub-agents");
        node.getConfig().put("orchestrationType", "SEQUENTIAL");
        node.getConfig().put("coordinationStrategy", "CENTRALIZED");
        workflow.getNodes().add(0, node);
    }

    private void addHitlNode(ProjectDescriptor descriptor) {
        if (descriptor.getWorkflows().isEmpty()) return;
        var workflow = descriptor.getWorkflows().get(0);
        var node = new NodeDescriptor();
        node.setId("hitl-1");
        node.setType("hitl-human-task");
        node.setExecutor("hitl");
        node.setDescription("Pause and wait for human approval before continuing");
        node.getConfig().put("taskType", "APPROVAL");
        workflow.getNodes().add(node);
        if (workflow.getNodes().size() > 1) {
            String lastNodeId = workflow.getNodes().get(workflow.getNodes().size() - 2).getId();
            workflow.getConnections().add(new ConnectionDescriptor(lastNodeId, "hitl-1"));
        }
    }
}
