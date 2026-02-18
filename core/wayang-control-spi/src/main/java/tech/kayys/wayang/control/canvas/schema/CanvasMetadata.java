package tech.kayys.wayang.control.canvas.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import lombok.Data;

/**
 * Metadata associated with a canvas.
 */
@Data
public class CanvasMetadata {
    public String title;
    public String author;
    public String license;
    public Map<String, String> customProperties = new HashMap<>();
    public Collaborator owner;
    public List<Collaborator> collaborators = new ArrayList<>();
}
