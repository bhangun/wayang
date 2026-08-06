package tech.kayys.wayang.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import tech.kayys.wayang.configuration.ConfigurationResource;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Default Database Service Implementation
 */
public class DefaultDatabaseService implements DatabaseService {
    
    private final String id;
    private final String name;
    private final Version version;
    private final Metadata metadata;
    private final HikariDataSource dataSource;
    private final DatabaseConfig config;
    private final ConcurrentHashMap<Thread, Connection> threadConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Thread, Integer> transactionDepth = new ConcurrentHashMap<>();
    private final List<Migration> appliedMigrations = new CopyOnWriteArrayList<>();
    private volatile boolean initialized = false;
    private volatile boolean healthy = true;
    
    public DefaultDatabaseService(DatabaseConfig config) {
        this.id = Id.random().asString();
        this.name = "database-service";
        this.version = Version.VERSION_1_0_0;
        this.config = config;
        this.metadata = Metadata.builder()
            .name(name)
            .description("Wayang Database Service")
            .version(version)
            .label("driver", config.driver())
            .label("url", maskUrl(config.url()))
            .now()
            .build();
        this.dataSource = createDataSource(config);
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version.toString(); }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("database"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(null);
    }
    
    @Override
    public Connection getConnection(String tenantId) throws SQLException {
        Connection conn = dataSource.getConnection();
        if (tenantId != null && !tenantId.isEmpty()) {
            // Set tenant context
            conn.createStatement().execute("SET app.tenant_id = '" + tenantId + "'");
        }
        return conn;
    }
    
    @Override
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Log but ignore
            }
        }
    }
    
    @Override
    public void beginTransaction() throws SQLException {
        beginTransaction(TransactionIsolation.READ_COMMITTED);
    }
    
    @Override
    public void beginTransaction(TransactionIsolation isolation) throws SQLException {
        Thread thread = Thread.currentThread();
        Connection conn = threadConnections.get(thread);
        
        if (conn == null) {
            conn = getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(isolation.getLevel());
            threadConnections.put(thread, conn);
            transactionDepth.put(thread, 1);
        } else {
            transactionDepth.compute(thread, (k, v) -> v == null ? 1 : v + 1);
        }
    }
    
    @Override
    public void commit() throws SQLException {
        Thread thread = Thread.currentThread();
        Connection conn = threadConnections.get(thread);
        
        if (conn == null) {
            throw new SQLException("No transaction in progress");
        }
        
        Integer depth = transactionDepth.get(thread);
        if (depth == null || depth <= 1) {
            conn.commit();
            conn.setAutoCommit(true);
            threadConnections.remove(thread);
            transactionDepth.remove(thread);
        } else {
            transactionDepth.put(thread, depth - 1);
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        Thread thread = Thread.currentThread();
        Connection conn = threadConnections.get(thread);
        
        if (conn == null) {
            throw new SQLException("No transaction in progress");
        }
        
        conn.rollback();
        conn.setAutoCommit(true);
        threadConnections.remove(thread);
        transactionDepth.remove(thread);
    }
    
    @Override
    public boolean inTransaction() {
        return threadConnections.containsKey(Thread.currentThread());
    }
    
    @Override
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        Connection conn = getConnectionForThread();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(mapper.mapRow(rs, rowNum++));
                }
                return results;
            }
        } finally {
            closeIfNotInTransaction(conn);
        }
    }
    
    @Override
    public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> results = query(sql, mapper, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public int update(String sql, Object... params) throws SQLException {
        Connection conn = getConnectionForThread();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } finally {
            closeIfNotInTransaction(conn);
        }
    }
    
    @Override
    public long insert(String sql, Object... params) throws SQLException {
        Connection conn = getConnectionForThread();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, params);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return -1;
            }
        } finally {
            closeIfNotInTransaction(conn);
        }
    }
    
    @Override
    public <T> T insertAndGet(String sql, GeneratedKeyMapper<T> mapper, Object... params) throws SQLException {
        Connection conn = getConnectionForThread();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, params);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return mapper.mapGeneratedKeys(rs);
                }
                return null;
            }
        } finally {
            closeIfNotInTransaction(conn);
        }
    }
    
    @Override
    public int[] batchUpdate(String sql, List<Object[]> params) throws SQLException {
        Connection conn = getConnectionForThread();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object[] param : params) {
                setParameters(stmt, param);
                stmt.addBatch();
            }
            return stmt.executeBatch();
        } finally {
            closeIfNotInTransaction(conn);
        }
    }
    
    @Override
    public <T> List<T> batchInsert(String sql, List<Object[]> params, GeneratedKeyMapper<T> mapper) throws SQLException {
        Connection conn = getConnectionForThread();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            List<T> results = new ArrayList<>();
            for (Object[] param : params) {
                setParameters(stmt, param);
                stmt.addBatch();
            }
            stmt.executeBatch();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                while (rs.next()) {
                    results.add(mapper.mapGeneratedKeys(rs));
                }
            }
            return results;
        } finally {
            closeIfNotInTransaction(conn);
        }
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return conn.getMetaData();
        }
    }
    
    @Override
    public boolean tableExists(String tableName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, config.schema(), tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        }
    }
    
    @Override
    public List<String> listTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, config.schema(), "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        }
        return tables;
    }
    
    @Override
    public boolean isHealthy() {
        return isHealthy(5000);
    }
    
    @Override
    public boolean isHealthy(long timeoutMs) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                return conn.isValid((int) (timeoutMs / 1000));
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            return false;
        }
    }
    
    @Override
    public DatabaseConfig getConfig() {
        return config;
    }
    
    @Override
    public void reloadConfig(ConfigurationResource config) {
        DatabaseConfig newConfig = DatabaseConfig.fromConfiguration(config);
        // Update data source
        // In practice, this would be more complex
    }
    
    @Override
    public void migrate() throws Exception {
        // Use Flyway or Liquibase integration
        applyMigrations("latest");
    }
    
    @Override
    public void migrate(String version) throws Exception {
        applyMigrations(version);
    }
    
    @Override
    public List<Migration> getAppliedMigrations() throws Exception {
        // Query migration table
        return appliedMigrations;
    }
    
    @Override
    public void initialize() throws Exception {
        if (!initialized) {
            // Initialize connection pool
            dataSource.getConnection().close();
            initialized = true;
            // Run migrations
            if (config.autoMigrate()) {
                migrate();
            }
        }
    }
    
    @Override
    public void shutdown() throws Exception {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    private HikariDataSource createDataSource(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.url());
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());
        hikariConfig.setDriverClassName(config.driver());
        hikariConfig.setMaximumPoolSize(config.maxPoolSize());
        hikariConfig.setMinimumIdle(config.minIdle());
        hikariConfig.setConnectionTimeout(config.connectionTimeoutMs());
        hikariConfig.setIdleTimeout(config.idleTimeoutMs());
        hikariConfig.setMaxLifetime(config.maxLifetimeMs());
        hikariConfig.setPoolName("Wayang-HikariPool");
        hikariConfig.setLeakDetectionThreshold(10000);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(hikariConfig);
    }
    
    private Connection getConnectionForThread() throws SQLException {
        Thread thread = Thread.currentThread();
        Connection conn = threadConnections.get(thread);
        if (conn != null) {
            return conn;
        }
        return getConnection();
    }
    
    private void closeIfNotInTransaction(Connection conn) throws SQLException {
        if (!inTransaction() && conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
    
    private void applyMigrations(String version) throws Exception {
        // Flyway integration
        // In practice, this would use Flyway or Liquibase
        Migration migration = new Migration(
            Id.random().asString(),
            "initial",
            version,
            "Initial database migration",
            "CREATE TABLE wayang_config (id VARCHAR(36) PRIMARY KEY, key VARCHAR(255), value TEXT)",
            Instant.now()
        );
        appliedMigrations.add(migration);
    }
    
    private String maskUrl(String url) {
        // Remove credentials from URL for logging
        return url.replaceAll(":[^:@]*@", ":***@");
    }
}
