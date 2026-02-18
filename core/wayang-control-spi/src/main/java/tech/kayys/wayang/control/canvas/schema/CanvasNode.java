package tech.kayys.wayang.control.canvas.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import tech.kayys.wayang.node.dto.NodePort;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Canvas node with AI capabilities and configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CanvasNode {
    public String id;
    public String type;
    public String subType; // For specialized node types
    public String label;
    public Map<String, Object> config = new HashMap<>();
    public Position position;
    public Dimensions dimensions = defaultDimensions();
    public NodeStyle style = new NodeStyle();
    public List<NodePort> ports = new ArrayList<>();
    public NodeValidation validation;
    public Map<String, Object> ui = new HashMap<>();

    // Composite / Sub-workflow support
    public boolean isComposite = false;
    public String subWorkflowId;
    public String sourceType; // PROJECT, TEMPLATE

    // AI-enhanced features
    public List<String> aiSuggestions = new ArrayList<>();
    public Double complexityScore;
    public List<String> optimizationHints = new ArrayList<>();

    private static Dimensions defaultDimensions() {
        Dimensions dimensions = new Dimensions();
        dimensions.width = 200.0;
        dimensions.height = 100.0;
        return dimensions;
    }
}
