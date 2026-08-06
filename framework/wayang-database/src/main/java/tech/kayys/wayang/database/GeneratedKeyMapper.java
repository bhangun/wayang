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
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Generated Key Mapper
 */
@FunctionalInterface
public interface GeneratedKeyMapper<T> {
    T mapGeneratedKeys(ResultSet rs) throws SQLException;
}
