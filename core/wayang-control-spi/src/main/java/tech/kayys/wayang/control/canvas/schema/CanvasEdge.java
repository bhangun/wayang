package tech.kayys.wayang.control.canvas.schema;

import lombok.Data;

/**
 * Edge connecting nodes on the canvas.
 */
@Data
public class CanvasEdge {
    public String id;
    public String source;
    public String target;
    public String sourceHandle;
    public String targetHandle;
    public EdgeType type;
    public EdgeStyle style;
    public EdgeRouting routing;
    public String label;
}
