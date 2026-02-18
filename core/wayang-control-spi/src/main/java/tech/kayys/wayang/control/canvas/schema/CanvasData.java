package tech.kayys.wayang.control.canvas.schema;

import java.util.List;
import java.util.ArrayList;
import lombok.Data;

/**
 * Top-level structure of the canvas data.
 */
@Data
public class CanvasData {
    public List<CanvasNode> nodes = new ArrayList<>();
    public List<CanvasEdge> edges = new ArrayList<>();
    public List<CanvasGroup> groups = new ArrayList<>();
    public List<CanvasAnnotation> annotations = new ArrayList<>();
    public CanvasLayout layout;
}
