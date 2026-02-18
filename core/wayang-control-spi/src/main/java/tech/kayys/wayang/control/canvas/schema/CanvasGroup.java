package tech.kayys.wayang.control.canvas.schema;

import java.util.List;
import java.util.ArrayList;
import lombok.Data;

/**
 * A group of elements on the canvas.
 */
@Data
public class CanvasGroup {
    public String id;
    public String label;
    public List<String> childIds = new ArrayList<>();
    public Position position;
    public Dimensions dimensions;
    public GroupStyle style;
}
