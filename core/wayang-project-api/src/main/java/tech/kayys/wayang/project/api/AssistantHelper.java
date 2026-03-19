package tech.kayys.wayang.project.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to bridge Project Management with the Wayang Assistant.
 * 
 * Allows for contextual suggestions, design-time validation, and 
 * intent-to-project bridging.
 */
public class AssistantHelper {

    /**
     * Generate inline design suggestions for a project based on its current state.
     * 
     * @param descriptor The current project descriptor
     * @return List of strings containing design advice or suggestions
     */
    public static List<String> suggestImprovements(ProjectDescriptor descriptor) {
        List<String> suggestions = new ArrayList<>();
        
        if (descriptor.getWorkflows().isEmpty()) {
            suggestions.add("Add a workflow to start defining agent interactions.");
        }
        
        if (descriptor.getCapabilities().contains("rag") && !hasNodeOfType(descriptor, "retriever")) {
            suggestions.add("You've enabled RAG capability, but haven't added a Retriever node yet.");
        }
        
        if (descriptor.getCapabilities().contains("hitl") && !hasNodeOfType(descriptor, "hitl")) {
            suggestions.add("Human-in-the-loop is enabled. Consider adding an 'approval' node to your workflow.");
        }
        
        return suggestions;
    }

    private static boolean hasNodeOfType(ProjectDescriptor descriptor, String typePart) {
        return descriptor.getWorkflows().stream()
                .flatMap(w -> w.getNodes().stream())
                .anyMatch(n -> n.getType() != null && n.getType().toLowerCase().contains(typePart.toLowerCase()));
    }
}
