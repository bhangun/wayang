package tech.kayys.wayang.security;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import tech.kayys.wayang.core.Principal;

/**
 * Authentication Result
 */
public record AuthenticationResult(
    boolean authenticated,
    Principal principal,
    String token,
    String refreshToken,
    String message,
    long expiresIn
) {
    public static AuthenticationResult success(Principal principal, String token, String refreshToken, long expiresIn) {
        return new AuthenticationResult(true, principal, token, refreshToken, null, expiresIn);
    }
    
    public static AuthenticationResult failure(String message) {
        return new AuthenticationResult(false, null, null, null, message, 0);
    }
}
