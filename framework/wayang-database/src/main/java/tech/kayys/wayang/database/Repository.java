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


import java.util.*;
import java.lang.reflect.*;

/**
 * Generic Repository Pattern
 */
public interface Repository<T, ID> {
    
    T save(T entity) throws Exception;
    List<T> saveAll(List<T> entities) throws Exception;
    Optional<T> findById(ID id) throws Exception;
    List<T> findAll() throws Exception;
    List<T> findAll(PageRequest page) throws Exception;
    long count() throws Exception;
    void delete(T entity) throws Exception;
    void deleteById(ID id) throws Exception;
    void deleteAll() throws Exception;
    boolean existsById(ID id) throws Exception;
}
