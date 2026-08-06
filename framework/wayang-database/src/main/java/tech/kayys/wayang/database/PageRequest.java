package tech.kayys.wayang.database;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



/**
 * Page Request
 */
public record PageRequest(int page, int size, String sortBy, boolean ascending) {
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, null, true);
    }
    
    public static PageRequest of(int page, int size, String sortBy) {
        return new PageRequest(page, size, sortBy, true);
    }
    
    public int offset() {
        return page * size;
    }
}
