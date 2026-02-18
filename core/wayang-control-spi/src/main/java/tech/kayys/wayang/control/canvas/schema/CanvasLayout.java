package tech.kayys.wayang.control.canvas.schema;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Canvas auto-layout preferences.
 */
@Data
@NoArgsConstructor
public class CanvasLayout {
    public LayoutAlgorithm algorithm = LayoutAlgorithm.DAGRE;
    public LayoutDirection direction = LayoutDirection.TOP_TO_BOTTOM;
    public Double nodeSpacing = 80.0;
    public Double layerSpacing = 120.0;
    public boolean autoLayout = false;
}
