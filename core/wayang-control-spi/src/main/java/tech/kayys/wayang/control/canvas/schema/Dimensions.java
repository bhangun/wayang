package tech.kayys.wayang.control.canvas.schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 2D dimensions of an element.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dimensions {
    public double width;
    public double height;
}
