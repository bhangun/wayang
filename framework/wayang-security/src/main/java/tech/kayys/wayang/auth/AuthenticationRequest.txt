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


import java.util.Map;

/**
 * Authentication Request
 */
public record AuthenticationRequest(
    String type,
    Map<String, Object> credentials
) {
    public static AuthenticationRequest of(String username, String password) {
        return new AuthenticationRequest("password", 
            Map.of("username", username, "password", password));
    }
    
    public static AuthenticationRequest ofToken(String token) {
        return new AuthenticationRequest("token", Map.of("token", token));
    }
}