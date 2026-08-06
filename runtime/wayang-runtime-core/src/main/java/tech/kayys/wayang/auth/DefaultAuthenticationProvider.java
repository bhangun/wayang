package tech.kayys.wayang.auth;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.core.Principal;
import tech.kayys.wayang.core.PrincipalType;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.security.AuthenticationResult;

public class DefaultAuthenticationProvider implements AuthenticationProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, String> tokens = new ConcurrentHashMap<>();
    
    public DefaultAuthenticationProvider() {
        this.id = Id.random().asString();
        this.name = "default-auth-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Authentication Provider")
            .version(version)
            .label("type", "auth")
            .now()
            .build();
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("auth"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) throws Exception {
        String type = request.type();
        Map<String, Object> credentials = request.credentials();
        
        if ("password".equals(type)) {
            String username = (String) credentials.get("username");
            String password = (String) credentials.get("password");
            
            if ("admin".equals(username) && "admin123".equals(password)) {
                String token = "token-" + UUID.randomUUID();
                tokens.put(token, username);
                
                Principal principal = new Principal(
                    Id.random(),
                    username,
                    "admin@wayang.io",
                    PrincipalType.USER,
                    "default",
                    "default",
                    Map.of("roles", List.of("admin"))
                );
                
                return AuthenticationResult.success(principal, token, "refresh-" + token, 3600);
            }
            
            return AuthenticationResult.failure("Invalid credentials");
        }
        
        if ("token".equals(type)) {
            String token = (String) credentials.get("token");
            if (tokens.containsKey(token)) {
                String username = tokens.get(token);
                Principal principal = new Principal(
                    Id.random(),
                    username,
                    username + "@wayang.io",
                    PrincipalType.USER,
                    "default",
                    "default",
                    Map.of()
                );
                return AuthenticationResult.success(principal, token, null, 3600);
            }
            return AuthenticationResult.failure("Invalid token");
        }
        
        return AuthenticationResult.failure("Unsupported authentication type: " + type);
    }
}

