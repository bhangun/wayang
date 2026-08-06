package tech.kayys.wayang.auth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



/**
 * Authentication Provider - authenticates users.
 */
public interface AuthenticationProvider extends Extension {

    /**
     * Authenticate with credentials
     */
    AuthenticationResult authenticate(AuthenticationRequest request) throws Exception;

    /**
     * Authenticate with token
     */
    default AuthenticationResult authenticate(String token) throws Exception {
        return authenticate(new AuthenticationRequest("token", Map.of("token", token)));
    }

    /**
     * Authenticate with OAuth2
     */
    default AuthenticationResult authenticateWithOAuth2(String provider, String code) throws Exception {
        return authenticate(new AuthenticationRequest("oauth2",
                Map.of("provider", provider, "code", code)));
    }

    /**
     * Refresh authentication
     */
    default AuthenticationResult refresh(String refreshToken) throws Exception {
        return authenticate(new AuthenticationRequest("refresh",
                Map.of("refreshToken", refreshToken)));
    }

    /**
     * Invalidate authentication
     */
    default void invalidate(String token) throws Exception {
        // Optional
    }
}