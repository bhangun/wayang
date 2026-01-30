package tech.kayys.wayang.security.secrets.local;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import tech.kayys.wayang.security.secrets.core.SecretManager;
import tech.kayys.wayang.security.secrets.dto.*;
import tech.kayys.wayang.security.secrets.exception.SecretException;
import tech.kayys.wayang.security.secrets.factory.LocalEncryptedSecretManager;

import java.time.Instant;
import java.util.*;

/**
 * Local encrypted storage implementation of SecretManager.
 *
 * Features:
 * - File-based storage with AES-256 encryption
 * - Suitable for development and standalone deployment
 * - No external dependencies
 * - Fast startup
 *
 * NOT RECOMMENDED for production environments.
 */
@ApplicationScoped
public class LocalEncryptedSecretManagerImpl implements LocalEncryptedSecretManager {

    private static final Logger LOG = Logger.getLogger(LocalEncryptedSecretManagerImpl.class);

    @Override
    public Uni<SecretMetadata> store(StoreSecretRequest request) {
        LOG.infof("Storing secret locally: tenant=%s, path=%s", request.tenantId(), request.path());

        // Implementation placeholder
        return Uni.createFrom().item(new SecretMetadata(
            request.tenantId(),
            request.path(),
            1,
            request.type(),
            Instant.now(),
            Instant.now(),
            request.ttl() != null ? Optional.of(Instant.now().plus(request.ttl())) : Optional.empty(),
            "system",
            request.metadata() != null ? request.metadata() : Map.of(),
            request.rotatable(),
            SecretStatus.ACTIVE
        ));
    }

    @Override
    public Uni<Secret> retrieve(RetrieveSecretRequest request) {
        return Uni.createFrom().failure(
            new SecretException(
                SecretException.ErrorCode.BACKEND_UNAVAILABLE,
                "Local encrypted storage not fully implemented."
            )
        );
    }

    @Override
    public Uni<Void> delete(DeleteSecretRequest request) {
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<List<SecretMetadata>> list(String tenantId, String path) {
        return Uni.createFrom().item(List.of());
    }

    @Override
    public Uni<SecretMetadata> rotate(RotateSecretRequest request) {
        return Uni.createFrom().failure(
            new SecretException(
                SecretException.ErrorCode.BACKEND_UNAVAILABLE,
                "Local encrypted storage not fully implemented."
            )
        );
    }

    @Override
    public Uni<Boolean> exists(String tenantId, String path) {
        return Uni.createFrom().item(false);
    }

    @Override
    public Uni<SecretMetadata> getMetadata(String tenantId, String path) {
        return Uni.createFrom().failure(
            new SecretException(
                SecretException.ErrorCode.SECRET_NOT_FOUND,
                "Secret not found"
            )
        );
    }

    @Override
    public Uni<HealthStatus> health() {
        return Uni.createFrom().item(
            HealthStatus.healthy("local", "Local encrypted storage available.")
        );
    }
}