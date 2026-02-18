package tech.kayys.wayang.control.canvas.schema;

import lombok.Data;
import java.time.Instant;

/**
 * A comment on the canvas.
 */
@Data
public class Comment {
    public String id;
    public String userId;
    public String text;
    public Position position;
    public Instant createdAt;
    public Instant updatedAt;
}
