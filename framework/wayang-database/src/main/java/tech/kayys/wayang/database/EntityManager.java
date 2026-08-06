package tech.kayys.wayang.database;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entity Manager - manages entity lifecycle
 */
public class EntityManager {
    
    private final DatabaseService db;
    private final Map<Class<?>, EntityMetadata> metadataCache = new ConcurrentHashMap<>();
    
    public EntityManager(DatabaseService db) {
        this.db = db;
    }
    
    public <T> T find(Class<T> entityClass, Object id) throws Exception {
        EntityMetadata meta = getMetadata(entityClass);
        String sql = "SELECT * FROM " + meta.tableName() + " WHERE " + meta.idColumn() + " = ?";
        return db.queryOne(sql, (rs, rowNum) -> mapRow(rs, entityClass, meta), id).orElse(null);
    }
    
    public <T> List<T> findAll(Class<T> entityClass) throws Exception {
        EntityMetadata meta = getMetadata(entityClass);
        String sql = "SELECT * FROM " + meta.tableName();
        return db.query(sql, (rs, rowNum) -> mapRow(rs, entityClass, meta));
    }
    
    public <T> T save(T entity) throws Exception {
        Class<?> entityClass = entity.getClass();
        EntityMetadata meta = getMetadata(entityClass);
        
        Object id = meta.getId(entity);
        if (id == null || (id instanceof Number && ((Number) id).longValue() == 0)) {
            // Insert
            return insert(entity, meta);
        } else {
            // Update
            return update(entity, meta);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T insert(T entity, EntityMetadata meta) throws Exception {
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        
        for (Field field : meta.fields()) {
            field.setAccessible(true);
            Object value = field.get(entity);
            if (value != null || field.isAnnotationPresent(Id.class)) {
                columns.add(meta.getColumnName(field));
                values.add(value);
                placeholders.add("?");
            }
        }
        
        String sql = "INSERT INTO " + meta.tableName() + 
            " (" + String.join(", ", columns) + ") VALUES (" + 
            String.join(", ", placeholders) + ")";
        
        long generatedId = db.insert(sql, values.toArray());
        
        // Set generated ID
        if (meta.idField() != null && generatedId > 0) {
            meta.idField().setAccessible(true);
            if (meta.idField().getType() == Long.class || meta.idField().getType() == long.class) {
                meta.idField().set(entity, generatedId);
            } else if (meta.idField().getType() == Integer.class || meta.idField().getType() == int.class) {
                meta.idField().set(entity, (int) generatedId);
            } else if (meta.idField().getType() == String.class) {
                meta.idField().set(entity, String.valueOf(generatedId));
            }
        }
        
        return entity;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T update(T entity, EntityMetadata meta) throws Exception {
        List<String> updates = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        
        for (Field field : meta.fields()) {
            if (field.isAnnotationPresent(Id.class)) continue;
            field.setAccessible(true);
            Object value = field.get(entity);
            updates.add(meta.getColumnName(field) + " = ?");
            values.add(value);
        }
        
        Object id = meta.getId(entity);
        values.add(id);
        
        String sql = "UPDATE " + meta.tableName() + " SET " + 
            String.join(", ", updates) + " WHERE " + meta.idColumn() + " = ?";
        
        db.update(sql, values.toArray());
        return entity;
    }
    
    private <T> T mapRow(ResultSet rs, Class<T> entityClass, EntityMetadata meta) throws SQLException {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            
            for (Field field : meta.fields()) {
                field.setAccessible(true);
                String columnName = meta.getColumnName(field);
                Object value = rs.getObject(columnName);
                if (value != null) {
                    field.set(entity, value);
                }
            }
            
            return entity;
        } catch (Exception e) {
            throw new SQLException("Failed to map row to entity " + entityClass.getName(), e);
        }
    }
    
    private EntityMetadata getMetadata(Class<?> entityClass) {
        return metadataCache.computeIfAbsent(entityClass, this::buildMetadata);
    }
    
    private EntityMetadata buildMetadata(Class<?> entityClass) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnnotation != null ? entityAnnotation.tableName() : 
            entityClass.getSimpleName().toLowerCase();
        
        List<Field> fields = new ArrayList<>();
        Field idField = null;
        String idColumn = "id";
        
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Transient.class)) continue;
            
            if (field.isAnnotationPresent(Id.class)) {
                idField = field;
                Column col = field.getAnnotation(Column.class);
                idColumn = col != null && !col.name().isEmpty() ? col.name() : field.getName();
            }
            
            fields.add(field);
        }
        
        return new EntityMetadata(entityClass, tableName, fields, idField, idColumn);
    }
}