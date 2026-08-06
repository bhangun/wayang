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


import java.util.*;
import java.time.*;
import java.security.*;
import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.*;

/**
 * Security Service
 */
public interface SecurityService {
    
    // Authentication
    AuthenticationResult authenticate(String username, String password) throws Exception;
    AuthenticationResult authenticate(String token) throws Exception;
    AuthenticationResult authenticateWithOAuth2(String provider, String code) throws Exception;
    
    // Token management
    String generateToken(Principal principal) throws Exception;
    String generateToken(Principal principal, Duration validity) throws Exception;
    String refreshToken(String refreshToken) throws Exception;
    void invalidateToken(String token) throws Exception;
    boolean validateToken(String token) throws Exception;
    Principal getPrincipalFromToken(String token) throws Exception;
    
    // Authorization
    AuthorizationResult authorize(Principal principal, Resource resource, String action) throws Exception;
    AuthorizationResult authorize(Principal principal, String resourceType, String resourceId, String action) throws Exception;
    boolean hasPermission(Principal principal, Permission permission) throws Exception;
    Set<Permission> getPermissions(Principal principal) throws Exception;
    
    // User management
    User createUser(User user) throws Exception;
    User updateUser(User user) throws Exception;
    void deleteUser(String userId) throws Exception;
    Optional<User> getUser(String userId) throws Exception;
    Optional<User> getUserByUsername(String username) throws Exception;
    List<User> listUsers() throws Exception;
    void assignRole(String userId, String roleId) throws Exception;
    void removeRole(String userId, String roleId) throws Exception;
    
    // Role management
    Role createRole(Role role) throws Exception;
    void deleteRole(String roleId) throws Exception;
    Optional<Role> getRole(String roleId) throws Exception;
    List<Role> listRoles() throws Exception;
    void assignPermission(String roleId, Permission permission) throws Exception;
    void removePermission(String roleId, Permission permission) throws Exception;
    
    // Password management
    void changePassword(String userId, String oldPassword, String newPassword) throws Exception;
    void resetPassword(String userId) throws Exception;
    void forcePasswordReset(String userId) throws Exception;
    
    // MFA
    void enableMFA(String userId) throws Exception;
    void disableMFA(String userId) throws Exception;
    boolean verifyMFA(String userId, String code) throws Exception;
    String generateMFASecret(String userId) throws Exception;
}
