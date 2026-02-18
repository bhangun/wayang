package tech.kayys.wayang.control.canvas.schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 2D position on the canvas.
 */
@Data
@NoArgsConstructor
public class Position {
    public double x;
    public double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
