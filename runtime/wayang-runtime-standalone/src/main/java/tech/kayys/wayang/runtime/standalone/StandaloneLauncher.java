package tech.kayys.wayang.runtime.standalone;

import io.quarkus.runtime.Quarkus;

public final class StandaloneLauncher {

    private static final String COMMUNITY_PROFILE = "community";
    private static final String H2_JDBC_URL =
            "jdbc:h2:mem:wayangdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    private StandaloneLauncher() {}

    public static void main(String... args) {
        String profiles = System.getProperty("quarkus.profile");
        if (profiles == null || profiles.isBlank()) {
            profiles = System.getenv("QUARKUS_PROFILE");
        }
        if (profiles == null || profiles.isBlank()) {
            profiles = COMMUNITY_PROFILE;
            System.setProperty("quarkus.profile", COMMUNITY_PROFILE);
        }

        if (profiles.contains(COMMUNITY_PROFILE)) {
            System.setProperty("quarkus.datasource.db-kind", System.getProperty("quarkus.datasource.db-kind", "h2"));
            System.setProperty("quarkus.datasource.jdbc.url", System.getProperty("quarkus.datasource.jdbc.url", H2_JDBC_URL));
            System.setProperty("quarkus.datasource.jdbc.driver", System.getProperty("quarkus.datasource.jdbc.driver", "org.h2.Driver"));
            System.setProperty("quarkus.datasource.username", System.getProperty("quarkus.datasource.username", "sa"));
            System.setProperty("quarkus.datasource.password", System.getProperty("quarkus.datasource.password", ""));
            System.setProperty("quarkus.datasource.reactive", System.getProperty("quarkus.datasource.reactive", "false"));
            System.setProperty("quarkus.hibernate-orm.schema-management.strategy",
                    System.getProperty("quarkus.hibernate-orm.schema-management.strategy", "drop-and-create"));
            System.setProperty("quarkus.hibernate-orm.sql-load-script",
                    System.getProperty("quarkus.hibernate-orm.sql-load-script", "no-file"));
            System.setProperty("quarkus.hibernate-orm.mapping.format.global",
                    System.getProperty("quarkus.hibernate-orm.mapping.format.global", "ignore"));
            System.setProperty("gamelan.tenant.default-id",
                    System.getProperty("gamelan.tenant.default-id", "community"));
            System.setProperty("gamelan.tenant.allow-default",
                    System.getProperty("gamelan.tenant.allow-default", "true"));
            System.setProperty("mp.jwt.verify.publickey.location",
                    System.getProperty("mp.jwt.verify.publickey.location", "classpath:jwt/public-key.pem"));
            System.setProperty("quarkus.langchain4j.ai.gemini.api-key",
                    System.getProperty("quarkus.langchain4j.ai.gemini.api-key", "community-default-key"));
        }

        Quarkus.run(StandaloneRuntime.class, args);
    }
}
