package tech.kayys.wayang.runtime.standalone.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * High-priority defaults for standalone mode.
 *
 * This source guards against transitive module application.properties values
 * when building as an uber-jar.
 */
public class StandaloneDefaultsConfigSource implements ConfigSource {

    private static final String H2_JDBC_URL =
            "jdbc:h2:mem:wayangdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    private static final Map<String, String> PROPERTIES = new LinkedHashMap<>();

    static {
        PROPERTIES.put("quarkus.profile", "community");
        PROPERTIES.put("quarkus.datasource.db-kind", "h2");
        PROPERTIES.put("quarkus.datasource.username", "sa");
        PROPERTIES.put("quarkus.datasource.password", "");
        PROPERTIES.put("quarkus.datasource.jdbc.url", H2_JDBC_URL);
        PROPERTIES.put("quarkus.datasource.jdbc.driver", "org.h2.Driver");
        PROPERTIES.put("quarkus.datasource.reactive", "false");
        PROPERTIES.put("quarkus.http.root-path", "/");
        PROPERTIES.put("quarkus.http.non-application-root-path", "/q");
        PROPERTIES.put("quarkus.hibernate-orm.schema-management.strategy", "drop-and-create");
        PROPERTIES.put("quarkus.hibernate-orm.sql-load-script", "no-file");
        PROPERTIES.put("quarkus.hibernate-orm.dialect", "org.hibernate.dialect.H2Dialect");
        PROPERTIES.put("quarkus.hibernate-orm.mapping.format.global", "ignore");
        PROPERTIES.put("quarkus.swagger-ui.always-include", "true");
        PROPERTIES.put("quarkus.swagger-ui.path", "swagger-ui");
        PROPERTIES.put("quarkus.smallrye-openapi.path", "openapi");
        PROPERTIES.put("quarkus.cache.type", "caffeine");
        PROPERTIES.put("quarkus.cache.redis.executors.value-type", "java.lang.String");
        PROPERTIES.put("quarkus.redis.health.enabled", "false");
        PROPERTIES.put("gamelan.registry.persistence.type", "memory");
        PROPERTIES.put("quarkus.langchain4j.ai.gemini.api-key", "community-default-key");
        PROPERTIES.put("wayang.multitenancy.enabled", "true");
        PROPERTIES.put("gamelan.tenant.default-id", "community");
        PROPERTIES.put("gamelan.tenant.allow-default", "true");
    }

    @Override
    public Map<String, String> getProperties() {
        return PROPERTIES;
    }

    @Override
    public Set<String> getPropertyNames() {
        return PROPERTIES.keySet();
    }

    @Override
    public int getOrdinal() {
        return 1000;
    }

    @Override
    public String getValue(String propertyName) {
        return PROPERTIES.get(propertyName);
    }

    @Override
    public String getName() {
        return "wayang-standalone-defaults";
    }
}
