package tech.kayys.wayang.security;

import tech.kayys.wayang.core.Principal;

/**
 * Default Security Service Implementation
 */
public class DefaultSecurityService implements SecurityService {
    
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, String> tokens = new ConcurrentHashMap<>();
    private final Map<String, String> refreshTokens = new ConcurrentHashMap<>();
    private final Map<String, String> mfaSecrets = new ConcurrentHashMap<>();
    private final Map<String, List<String>> userMFASecrets = new ConcurrentHashMap<>();
    
    private final String jwtSecret;
    private final long tokenValiditySeconds;
    private final long refreshTokenValiditySeconds;
    private final Algorithm algorithm;
    private final JWTCreator.Builder tokenBuilder;
    
    public DefaultSecurityService(String jwtSecret, long tokenValiditySeconds, long refreshTokenValiditySeconds) {
        this.jwtSecret = jwtSecret;
        this.tokenValiditySeconds = tokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        this.algorithm = Algorithm.HMAC256(jwtSecret);
        this.tokenBuilder = JWT.create()
            .withIssuer("wayang")
            .withAudience("wayang-api");
        
        // Create default roles
        createDefaultRoles();
    }
    
    @Override
    public AuthenticationResult authenticate(String username, String password) throws Exception {
        User user = getUserByUsername(username).orElse(null);
        if (user == null || user.status() != UserStatus.ACTIVE) {
            return AuthenticationResult.failure("Invalid credentials");
        }
        
        // Verify password (in practice, use bcrypt)
        if (!password.equals(getPasswordHash(username))) {
            return AuthenticationResult.failure("Invalid credentials");
        }
        
        // Check MFA
        if (user.mfaEnabled()) {
            // In practice, this would require MFA verification
            return AuthenticationResult.failure("MFA required");
        }
        
        Principal principal = new Principal(
            Id.fromString(user.id()),
            user.username(),
            user.email(),
            PrincipalType.USER,
            "default",
            "default",
            Map.of("firstName", user.firstName(), "lastName", user.lastName(), "roles", user.roles())
        );
        
        String token = generateToken(principal);
        String refreshToken = generateRefreshToken(principal);
        
        // Store tokens
        tokens.put(token, user.id());
        refreshTokens.put(refreshToken, user.id());
        
        // Update last login
        user = new User(
            user.id(), user.username(), user.email(), user.firstName(), user.lastName(),
            user.status(), user.roles(), user.attributes(),
            user.createdAt(), Instant.now(), Instant.now(), user.mfaEnabled()
        );
        users.put(user.id(), user);
        
        return AuthenticationResult.success(principal, token, refreshToken, tokenValiditySeconds);
    }
    
    @Override
    public AuthenticationResult authenticate(String token) throws Exception {
        if (!validateToken(token)) {
            return AuthenticationResult.failure("Invalid token");
        }
        
        Principal principal = getPrincipalFromToken(token);
        return AuthenticationResult.success(principal, token, null, getRemainingValidity(token));
    }
    
    @Override
    public AuthenticationResult authenticateWithOAuth2(String provider, String code) throws Exception {
        // In practice, validate with OAuth2 provider
        // For now, create a test user
        User user = getUserByUsername("oauth_" + provider).orElseGet(() -> {
            User newUser = User.builder()
                .username("oauth_" + provider)
                .email("oauth@" + provider + ".example.com")
                .status(UserStatus.ACTIVE)
                .attribute("oauthProvider", provider)
                .build();
            users.put(newUser.id(), newUser);
            return newUser;
        });
        
        Principal principal = new Principal(
            Id.fromString(user.id()),
            user.username(),
            user.email(),
            PrincipalType.USER,
            "default",
            "default",
            Map.of("oauthProvider", provider)
        );
        
        String token = generateToken(principal);
        String refreshToken = generateRefreshToken(principal);
        tokens.put(token, user.id());
        refreshTokens.put(refreshToken, user.id());
        
        return AuthenticationResult.success(principal, token, refreshToken, tokenValiditySeconds);
    }
    
    @Override
    public String generateToken(Principal principal) throws Exception {
        return generateToken(principal, Duration.ofSeconds(tokenValiditySeconds));
    }
    
    @Override
    public String generateToken(Principal principal, Duration validity) throws Exception {
        return tokenBuilder
            .withSubject(principal.username())
            .withClaim("userId", principal.id().asString())
            .withClaim("email", principal.email())
            .withClaim("type", principal.type().name())
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + validity.toMillis()))
            .sign(algorithm);
    }
    
    @Override
    public String refreshToken(String refreshToken) throws Exception {
        String userId = refreshTokens.get(refreshToken);
        if (userId == null) {
            throw new SecurityException("Invalid refresh token");
        }
        
        User user = getUser(userId).orElseThrow(() -> new SecurityException("User not found"));
        
        Principal principal = new Principal(
            Id.fromString(user.id()),
            user.username(),
            user.email(),
            PrincipalType.USER,
            "default",
            "default",
            Map.of()
        );
        
        String newToken = generateToken(principal);
        tokens.put(newToken, userId);
        
        // Invalidate old token
        tokens.entrySet().removeIf(e -> e.getValue().equals(userId));
        
        return newToken;
    }
    
    @Override
    public void invalidateToken(String token) throws Exception {
        tokens.remove(token);
    }
    
    @Override
    public boolean validateToken(String token) throws Exception {
        try {
            JWT.require(algorithm)
                .withIssuer("wayang")
                .build()
                .verify(token);
            return tokens.containsKey(token);
        } catch (JWTVerificationException e) {
            return false;
        }
    }
    
    @Override
    public Principal getPrincipalFromToken(String token) throws Exception {
        DecodedJWT decoded = JWT.decode(token);
        String userId = decoded.getClaim("userId").asString();
        String username = decoded.getSubject();
        String email = decoded.getClaim("email").asString();
        
        return new Principal(
            Id.fromString(userId),
            username,
            email,
            PrincipalType.USER,
            "default",
            "default",
            Map.of()
        );
    }
    
    @Override
    public AuthorizationResult authorize(Principal principal, Resource resource, String action) throws Exception {
        return authorize(principal, resource.type().asString(), resource.id().asString(), action);
    }
    
    @Override
    public AuthorizationResult authorize(Principal principal, String resourceType, String resourceId, String action) throws Exception {
        User user = getUser(principal.id().asString()).orElse(null);
        if (user == null) {
            return AuthorizationResult.failure("User not found");
        }
        
        Set<Permission> userPermissions = getPermissions(principal);
        
        Permission required = Permission.of(resourceType + ":" + resourceId, action);
        if (userPermissions.contains(required)) {
            return AuthorizationResult.success();
        }
        
        // Check wildcard permissions
        Permission wildcard = Permission.of(resourceType + ":*", action);
        if (userPermissions.contains(wildcard)) {
            return AuthorizationResult.success();
        }
        
        return AuthorizationResult.failure("Permission denied", Set.of(required));
    }
    
    @Override
    public boolean hasPermission(Principal principal, Permission permission) throws Exception {
        Set<Permission> permissions = getPermissions(principal);
        return permissions.contains(permission);
    }
    
    @Override
    public Set<Permission> getPermissions(Principal principal) throws Exception {
        User user = getUser(principal.id().asString()).orElse(null);
        if (user == null) {
            return Set.of();
        }
        
        Set<Permission> permissions = new HashSet<>();
        for (String roleId : user.roles()) {
            Role role = getRole(roleId).orElse(null);
            if (role != null) {
                permissions.addAll(role.permissions());
            }
        }
        return permissions;
    }
    
    @Override
    public User createUser(User user) throws Exception {
        if (users.values().stream().anyMatch(u -> u.username().equals(user.username()))) {
            throw new IllegalArgumentException("Username already exists");
        }
        users.put(user.id(), user);
        return user;
    }
    
    @Override
    public User updateUser(User user) throws Exception {
        if (!users.containsKey(user.id())) {
            throw new IllegalArgumentException("User not found");
        }
        users.put(user.id(), user);
        return user;
    }
    
    @Override
    public void deleteUser(String userId) throws Exception {
        users.remove(userId);
        tokens.entrySet().removeIf(e -> e.getValue().equals(userId));
        refreshTokens.entrySet().removeIf(e -> e.getValue().equals(userId));
    }
    
    @Override
    public Optional<User> getUser(String userId) throws Exception {
        return Optional.ofNullable(users.get(userId));
    }
    
    @Override
    public Optional<User> getUserByUsername(String username) throws Exception {
        return users.values().stream()
            .filter(u -> u.username().equals(username))
            .findFirst();
    }
    
    @Override
    public List<User> listUsers() throws Exception {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public void assignRole(String userId, String roleId) throws Exception {
        User user = getUser(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<String> roles = new ArrayList<>(user.roles());
        if (!roles.contains(roleId)) {
            roles.add(roleId);
        }
        User updated = new User(
            user.id(), user.username(), user.email(), user.firstName(), user.lastName(),
            user.status(), roles, user.attributes(),
            user.createdAt(), Instant.now(), user.lastLoginAt(), user.mfaEnabled()
        );
        users.put(userId, updated);
    }
    
    @Override
    public void removeRole(String userId, String roleId) throws Exception {
        User user = getUser(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<String> roles = new ArrayList<>(user.roles());
        roles.remove(roleId);
        User updated = new User(
            user.id(), user.username(), user.email(), user.firstName(), user.lastName(),
            user.status(), roles, user.attributes(),
            user.createdAt(), Instant.now(), user.lastLoginAt(), user.mfaEnabled()
        );
        users.put(userId, updated);
    }
    
    @Override
    public Role createRole(Role role) throws Exception {
        if (roles.containsKey(role.id())) {
            throw new IllegalArgumentException("Role already exists");
        }
        roles.put(role.id(), role);
        return role;
    }
    
    @Override
    public void deleteRole(String roleId) throws Exception {
        roles.remove(roleId);
    }
    
    @Override
    public Optional<Role> getRole(String roleId) throws Exception {
        return Optional.ofNullable(roles.get(roleId));
    }
    
    @Override
    public List<Role> listRoles() throws Exception {
        return new ArrayList<>(roles.values());
    }
    
    @Override
    public void assignPermission(String roleId, Permission permission) throws Exception {
        Role role = getRole(roleId).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        List<Permission> permissions = new ArrayList<>(role.permissions());
        permissions.add(permission);
        Role updated = new Role(role.id(), role.name(), role.description(), permissions, role.systemRole());
        roles.put(roleId, updated);
    }
    
    @Override
    public void removePermission(String roleId, Permission permission) throws Exception {
        Role role = getRole(roleId).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        List<Permission> permissions = new ArrayList<>(role.permissions());
        permissions.remove(permission);
        Role updated = new Role(role.id(), role.name(), role.description(), permissions, role.systemRole());
        roles.put(roleId, updated);
    }
    
    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) throws Exception {
        User user = getUser(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        // In practice, verify old password
        setPasswordHash(user.username(), newPassword);
    }
    
    @Override
    public void resetPassword(String userId) throws Exception {
        // Generate random password and send to user
        String newPassword = UUID.randomUUID().toString().substring(0, 12);
        setPasswordHash(userId, newPassword);
    }
    
    @Override
    public void forcePasswordReset(String userId) throws Exception {
        User user = getUser(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User updated = new User(
            user.id(), user.username(), user.email(), user.firstName(), user.lastName(),
            UserStatus.PASSWORD_RESET, user.roles(), user.attributes(),
            user.createdAt(), Instant.now(), user.lastLoginAt(), user.mfaEnabled()
        );
        users.put(userId, updated);
    }
    
    @Override
    public void enableMFA(String userId) throws Exception {
        User user = getUser(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User updated = new User(
            user.id(), user.username(), user.email(), user.firstName(), user.lastName(),
            user.status(), user.roles(), user.attributes(),
            user.createdAt(), Instant.now(), user.lastLoginAt(), true
        );
        users.put(userId, updated);
    }
    
    @Override
    public void disableMFA(String userId) throws Exception {
        User user = getUser(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User updated = new User(
            user.id(), user.username(), user.email(), user.firstName(), user.lastName(),
            user.status(), user.roles(), user.attributes(),
            user.createdAt(), Instant.now(), user.lastLoginAt(), false
        );
        users.put(userId, updated);
    }
    
    @Override
    public boolean verifyMFA(String userId, String code) throws Exception {
        // In practice, verify with TOTP
        return true;
    }
    
    @Override
    public String generateMFASecret(String userId) throws Exception {
        String secret = UUID.randomUUID().toString().substring(0, 16);
        mfaSecrets.put(userId, secret);
        return secret;
    }
    
    // === Helper Methods ===
    
    private void createDefaultRoles() {
        // Admin role
        Role admin = Role.builder()
            .id("admin")
            .name("Administrator")
            .description("Full system access")
            .permission(Permission.of("*:*", "*"))
            .systemRole(true)
            .build();
        roles.put("admin", admin);
        
        // Manager role
        Role manager = Role.builder()
            .id("manager")
            .name("Manager")
            .description("Manage resources")
            .permission(Permission.of("agent:*", "manage"))
            .permission(Permission.of("execution:*", "manage"))
            .permission(Permission.of("tenant:*", "view"))
            .systemRole(true)
            .build();
        roles.put("manager", manager);
        
        // User role
        Role user = Role.builder()
            .id("user")
            .name("User")
            .description("Basic user access")
            .permission(Permission.of("agent:*", "view"))
            .permission(Permission.of("execution:*", "execute"))
            .systemRole(true)
            .build();
        roles.put("user", user);
        
        // Create admin user
        User adminUser = User.builder()
            .username("admin")
            .email("admin@wayang.io")
            .firstName("Admin")
            .lastName("User")
            .status(UserStatus.ACTIVE)
            .role("admin")
            .build();
        users.put(adminUser.id(), adminUser);
        setPasswordHash("admin", "admin123");
    }
    
    private String generateRefreshToken(Principal principal) throws Exception {
        return JWT.create()
            .withSubject(principal.username())
            .withClaim("userId", principal.id().asString())
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValiditySeconds * 1000))
            .sign(algorithm);
    }
    
    private long getRemainingValidity(String token) {
        try {
            DecodedJWT decoded = JWT.decode(token);
            Date expiresAt = decoded.getExpiresAt();
            if (expiresAt != null) {
                return (expiresAt.getTime() - System.currentTimeMillis()) / 1000;
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }
    
    // In practice, these would use bcrypt
    private final Map<String, String> passwordHashes = new ConcurrentHashMap<>();
    
    private String getPasswordHash(String username) {
        return passwordHashes.get(username);
    }
    
    private void setPasswordHash(String username, String password) {
        // In practice, use bcrypt.hashpw(password, BCrypt.gensalt())
        passwordHashes.put(username, password);
    }
}