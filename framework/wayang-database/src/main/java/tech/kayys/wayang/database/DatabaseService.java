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


import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.*;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import tech.kayys.wayang.configuration.ConfigurationResource;

/**
 * Database Service - Unified database access layer
 */
public interface DatabaseService extends Extension {
    
    // Connection management
    Connection getConnection() throws SQLException;
    Connection getConnection(String tenantId) throws SQLException;
    void closeConnection(Connection connection);
    
    // Transaction management
    void beginTransaction() throws SQLException;
    void beginTransaction(TransactionIsolation isolation) throws SQLException;
    void commit() throws SQLException;
    void rollback() throws SQLException;
    boolean inTransaction();
    
    // Query execution
    <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws SQLException;
    <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException;
    int update(String sql, Object... params) throws SQLException;
    long insert(String sql, Object... params) throws SQLException;
    <T> T insertAndGet(String sql, GeneratedKeyMapper<T> mapper, Object... params) throws SQLException;
    
    // Batch operations
    int[] batchUpdate(String sql, List<Object[]> params) throws SQLException;
    <T> List<T> batchInsert(String sql, List<Object[]> params, GeneratedKeyMapper<T> mapper) throws SQLException;
    
    // Database metadata
    DatabaseMetaData getMetaData() throws SQLException;
    boolean tableExists(String tableName) throws SQLException;
    List<String> listTables() throws SQLException;
    
    // Health check
    boolean isHealthy();
    boolean isHealthy(long timeoutMs);
    
    // Configuration
    DatabaseConfig getConfig();
    void reloadConfig(ConfigurationResource config);
    
    // Migration
    void migrate() throws Exception;
    void migrate(String version) throws Exception;
    List<Migration> getAppliedMigrations() throws Exception;
}

