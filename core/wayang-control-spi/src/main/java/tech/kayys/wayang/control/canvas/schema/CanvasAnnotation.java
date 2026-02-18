package tech.kayys.wayang.control.canvas.schema;

import lombok.Data;

/**
 * Annotation element on the canvas (notes, labels).
 */
@Data
public class CanvasAnnotation {
    public String id;
    public String text;
    public Position position;
    public Dimensions dimensions;
    public AnnotationStyle style;
}
