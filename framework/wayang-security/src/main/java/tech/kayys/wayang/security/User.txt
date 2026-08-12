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


import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;


/**
 * User
 */
public record User(
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
    UserStatus status,
    List<String> roles,
    Map<String, Object> attributes,
    Instant createdAt,
    Instant updatedAt,
    Instant lastLoginAt,
    boolean mfaEnabled
) {
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    public static class UserBuilder {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private UserStatus status = UserStatus.ACTIVE;
        private final List<String> roles = new ArrayList<>();
        private final Map<String, Object> attributes = new HashMap<>();
        private Instant createdAt;
        private Instant updatedAt;
        private Instant lastLoginAt;
        private boolean mfaEnabled;
        
        public UserBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public UserBuilder status(UserStatus status) {
            this.status = status;
            return this;
        }
        
        public UserBuilder role(String role) {
            this.roles.add(role);
            return this;
        }
        
        public UserBuilder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }
        
        public UserBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public UserBuilder lastLoginAt(Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }
        
        public UserBuilder mfaEnabled(boolean mfaEnabled) {
            this.mfaEnabled = mfaEnabled;
            return this;
        }
        
        public User build() {
            if (id == null) {
                id = Id.random().asString();
            }
            if (createdAt == null) {
                createdAt = Instant.now();
            }
            if (updatedAt == null) {
                updatedAt = createdAt;
            }
            return new User(id, username, email, firstName, lastName, status, roles, attributes, 
                createdAt, updatedAt, lastLoginAt, mfaEnabled);
        }
    }
}
