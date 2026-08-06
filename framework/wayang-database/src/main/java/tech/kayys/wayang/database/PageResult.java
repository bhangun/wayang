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


import java.util.List;

/**
 * Page Result
 */
public record PageResult<T>(List<T> content, long totalElements, int totalPages, int currentPage) {
    public static <T> PageResult<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(content, totalElements, totalPages, page);
    }
}
