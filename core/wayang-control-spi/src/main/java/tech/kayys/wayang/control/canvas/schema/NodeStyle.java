package tech.kayys.wayang.control.canvas.schema;

import lombok.Data;

/**
 * Visual style of a node.
 */
@Data
public class NodeStyle {
    public String backgroundColor;
    public String borderColor;
    public String textColor;
    public double borderRadius;
    public double borderWidth;
    public String fontSize;
}
