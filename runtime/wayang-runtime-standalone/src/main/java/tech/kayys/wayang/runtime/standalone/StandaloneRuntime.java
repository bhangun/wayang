package tech.kayys.wayang.runtime.standalone;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import java.util.Arrays;
import org.jboss.logging.Logger;
import tech.kayys.wayang.runtime.standalone.status.RuntimeStatusSnapshot;
import tech.kayys.wayang.runtime.standalone.status.RuntimeStatusService;

@QuarkusMain
public class StandaloneRuntime implements QuarkusApplication {

    private static final Logger LOG = Logger.getLogger(StandaloneRuntime.class);
    private static final String COMMUNITY_PROFILE = "community";
    private static final String COMMUNITY_TENANT_ID = "community";
    private static final String COMMUNITY_ALLOW_DEFAULT = "true";
    private static final String H2_JDBC_URL =
            "jdbc:h2:mem:wayangdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    static {
        // Default to community profile for portable standalone runs.
        String profiles = System.getProperty("quarkus.profile");
        if (profiles == null || profiles.isBlank()) {
            profiles = System.getenv("QUARKUS_PROFILE");
        }
        if (profiles == null || profiles.isBlank()) {
            profiles = COMMUNITY_PROFILE;
            System.setProperty("quarkus.profile", COMMUNITY_PROFILE);
        }

        boolean communityMode = Arrays.stream(profiles.split(","))
                .map(String::trim)
                .anyMatch(COMMUNITY_PROFILE::equalsIgnoreCase);
        if (communityMode) {
            // Ensure community mode always has tenant fallbacks during early Quarkus bootstrap.
            if (System.getProperty("gamelan.tenant.default-id") == null) {
                System.setProperty("gamelan.tenant.default-id", COMMUNITY_TENANT_ID);
            }
            if (System.getProperty("gamelan.tenant.allow-default") == null) {
                System.setProperty("gamelan.tenant.allow-default", COMMUNITY_ALLOW_DEFAULT);
            }
            if (System.getProperty("mp.jwt.verify.publickey.location") == null) {
                System.setProperty("mp.jwt.verify.publickey.location", "classpath:jwt/public-key.pem");
            }
            // Force standalone community defaults to win over transitive module properties.
            if (System.getProperty("quarkus.datasource.db-kind") == null) {
                System.setProperty("quarkus.datasource.db-kind", "h2");
            }
            if (System.getProperty("quarkus.datasource.jdbc.url") == null) {
                System.setProperty("quarkus.datasource.jdbc.url", H2_JDBC_URL);
            }
            if (System.getProperty("quarkus.datasource.jdbc.driver") == null) {
                System.setProperty("quarkus.datasource.jdbc.driver", "org.h2.Driver");
            }
            if (System.getProperty("quarkus.datasource.username") == null) {
                System.setProperty("quarkus.datasource.username", "sa");
            }
            if (System.getProperty("quarkus.datasource.password") == null) {
                System.setProperty("quarkus.datasource.password", "");
            }
            if (System.getProperty("quarkus.hibernate-orm.schema-management.strategy") == null) {
                System.setProperty("quarkus.hibernate-orm.schema-management.strategy", "drop-and-create");
            }
            if (System.getProperty("quarkus.hibernate-orm.sql-load-script") == null) {
                System.setProperty("quarkus.hibernate-orm.sql-load-script", "no-file");
            }
            if (System.getProperty("quarkus.hibernate-orm.mapping.format.global") == null) {
                System.setProperty("quarkus.hibernate-orm.mapping.format.global", "ignore");
            }
        }
    }

    @Inject RuntimeStatusService runtimeStatusService;

    @Override
    public int run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("      WAYANG STANDALONE RUNTIME STARTING         ");
        System.out.println("=================================================");
        LOG.infof("Active quarkus.profile=%s", System.getProperty("quarkus.profile", "community"));

        RuntimeStatusSnapshot snapshot = runtimeStatusService.collectStatus().await().indefinitely();
        if (snapshot.ready()) {
            LOG.infof("Wayang standalone is ready. Components=%s", snapshot.components());
        } else {
            LOG.warnf("Wayang standalone started with degraded components=%s", snapshot.components());
        }

        // 7. Start Quarkus and wait
        System.out.println("=================================================");
        System.out.println("      WAYANG STANDALONE RUNTIME READY            ");
        System.out.println("=================================================");

        Quarkus.waitForExit();
        return 0;
    }
}
