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


import java.lang.reflect.Field;
import java.util.List;

/**
 * Entity Metadata
 */
public record EntityMetadata(
    Class<?> entityClass,
    String tableName,
    List<Field> fields,
    Field idField,
    String idColumn
) {
    public String getColumnName(Field field) {
        Column col = field.getAnnotation(Column.class);
        return col != null && !col.name().isEmpty() ? col.name() : field.getName();
    }
    
    public Object getId(Object entity) throws IllegalAccessException {
        if (idField == null) return null;
        idField.setAccessible(true);
        return idField.get(entity);
    }
}
