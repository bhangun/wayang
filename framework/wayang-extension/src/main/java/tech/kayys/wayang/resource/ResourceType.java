package tech.kayys.wayang.resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Objects;

/**
 * Represents the resource type.
 * Using a interface allows exhaustive pattern matching.
 */


/**
 * Represents the resource type.
 * Using a interface allows exhaustive pattern matching.
 */
public interface ResourceType {
    
    // ============================================================================
    // Foundation Types
    // ============================================================================
    
    record Resource() implements ResourceType {
        @Override
        public String name() { return "resource"; }
    }
    
    record Artifact() implements ResourceType {
        @Override
        public String name() { return "artifact"; }
    }
    
    record Event() implements ResourceType {
        @Override
        public String name() { return "event"; }
    }
    
    record Message() implements ResourceType {
        @Override
        public String name() { return "message"; }
    }
    
    // ============================================================================
    // Definition Types
    // ============================================================================
    
    record Agent() implements ResourceType {
        @Override
        public String name() { return "agent"; }
    }
    
    record Skill() implements ResourceType {
        @Override
        public String name() { return "skill"; }
    }
    
    record Tool() implements ResourceType {
        @Override
        public String name() { return "tool"; }
    }
    
    record Workflow() implements ResourceType {
        @Override
        public String name() { return "workflow"; }
    }
    
    record Prompt() implements ResourceType {
        @Override
        public String name() { return "prompt"; }
    }
    
    record Policy() implements ResourceType {
        @Override
        public String name() { return "policy"; }
    }
    
    record Capability() implements ResourceType {
        @Override
        public String name() { return "capability"; }
    }
    
    // ============================================================================
    // Runtime Types
    // ============================================================================
    
    record Execution() implements ResourceType {
        @Override
        public String name() { return "execution"; }
    }
    
    record Session() implements ResourceType {
        @Override
        public String name() { return "session"; }
    }
    
    record Instance() implements ResourceType {
        @Override
        public String name() { return "instance"; }
    }
    
    // ============================================================================
    // Storage Types
    // ============================================================================
    
    record Document() implements ResourceType {
        @Override
        public String name() { return "document"; }
    }
    
    record Dataset() implements ResourceType {
        @Override
        public String name() { return "dataset"; }
    }
    
    record Model() implements ResourceType {
        @Override
        public String name() { return "model"; }
    }
    
    // ============================================================================
    // Extension Types
    // ============================================================================
    
    record Plugin() implements ResourceType {
        @Override
        public String name() { return "plugin"; }
    }
    
    record Extension() implements ResourceType {
        @Override
        public String name() { return "extension"; }
    }
    
    record Descriptor() implements ResourceType {
        @Override
        public String name() { return "descriptor"; }
    }
    
    record Manifest() implements ResourceType {
        @Override
        public String name() { return "manifest"; }
    }
    
    // ============================================================================
    // Configuration Types
    // ============================================================================
    
    record Configuration() implements ResourceType {
        @Override
        public String name() { return "configuration"; }
    }
    
    // ============================================================================
    // Security Types
    // ============================================================================
    
    record User() implements ResourceType {
        @Override
        public String name() { return "user"; }
    }
    
    record Role() implements ResourceType {
        @Override
        public String name() { return "role"; }
    }
    
    record Permission() implements ResourceType {
        @Override
        public String name() { return "permission"; }
    }
    
    // ============================================================================
    // Messaging Types
    // ============================================================================
    
    record Topic() implements ResourceType {
        @Override
        public String name() { return "topic"; }
    }
    
    record Queue() implements ResourceType {
        @Override
        public String name() { return "queue"; }
    }
    
    // ============================================================================
    // Observability Types
    // ============================================================================
    
    record Trace() implements ResourceType {
        @Override
        public String name() { return "trace"; }
    }
    
    record Metric() implements ResourceType {
        @Override
        public String name() { return "metric"; }
    }
    
    record Log() implements ResourceType {
        @Override
        public String name() { return "log"; }
    }
    
    record Audit() implements ResourceType {
        @Override
        public String name() { return "audit"; }
    }
    
    // ============================================================================
    // Tenant Types
    // ============================================================================
    
    record Tenant() implements ResourceType {
        @Override
        public String name() { return "tenant"; }
    }
    
    record Namespace() implements ResourceType {
        @Override
        public String name() { return "namespace"; }
    }
    
    // ============================================================================
    // Custom Type
    // ============================================================================
    
    record Custom(String name) implements ResourceType {
        public Custom {
            Objects.requireNonNull(name, "name cannot be null");
        }
        
        @Override
        public String name() { return name; }
    }
    
    // ============================================================================
    // Interface Methods
    // ============================================================================
    
    /**
     * Get the name of the resource type.
     */
    String name();
    
    /**
     * Get the string representation of the resource type.
     */
    default String asString() {
        return name();
    }
    
    /**
     * Parse a resource type from a string.
     */
    static ResourceType fromString(String name) {
        if (name == null) {
            return new Custom("unknown");
        }
        
        return switch (name.toLowerCase()) {
            // Foundation types
            case "resource" -> new Resource();
            case "artifact" -> new Artifact();
            case "event" -> new Event();
            case "message" -> new Message();
            
            // Definition types
            case "agent" -> new Agent();
            case "skill" -> new Skill();
            case "tool" -> new Tool();
            case "workflow" -> new Workflow();
            case "prompt" -> new Prompt();
            case "policy" -> new Policy();
            case "capability" -> new Capability();
            
            // Runtime types
            case "execution" -> new Execution();
            case "session" -> new Session();
            case "instance" -> new Instance();
            
            // Storage types
            case "document" -> new Document();
            case "dataset" -> new Dataset();
            case "model" -> new Model();
            
            // Extension types
            case "plugin" -> new Plugin();
            case "extension" -> new Extension();
            case "descriptor" -> new Descriptor();
            case "manifest" -> new Manifest();
            
            // Configuration types
            case "configuration" -> new Configuration();
            
            // Security types
            case "user" -> new User();
            case "role" -> new Role();
            case "permission" -> new Permission();
            
            // Messaging types
            case "topic" -> new Topic();
            case "queue" -> new Queue();
            
            // Observability types
            case "trace" -> new Trace();
            case "metric" -> new Metric();
            case "log" -> new Log();
            case "audit" -> new Audit();
            
            // Tenant types
            case "tenant" -> new Tenant();
            case "namespace" -> new Namespace();
            
            // Custom
            default -> new Custom(name);
        };
    }
    
    /**
     * Check if this type matches another type by name.
     */
    default boolean matches(String name) {
        return this.name().equalsIgnoreCase(name);
    }
    
    /**
     * Check if this type is a custom type.
     */
    default boolean isCustom() {
        return this instanceof Custom;
    }
    
    /**
     * Check if this type is a foundation type.
     */
    default boolean isFoundation() {
        return this instanceof Resource || this instanceof Artifact || 
               this instanceof Event || this instanceof Message;
    }
    
    /**
     * Check if this type is a definition type.
     */
    default boolean isDefinition() {
        return this instanceof Agent || this instanceof Skill || 
               this instanceof Tool || this instanceof Workflow ||
               this instanceof Prompt || this instanceof Policy ||
               this instanceof Capability;
    }
    
    /**
     * Check if this type is a runtime type.
     */
    default boolean isRuntime() {
        return this instanceof Execution || this instanceof Session || 
               this instanceof Instance;
    }
    
    /**
     * Check if this type is a storage type.
     */
    default boolean isStorage() {
        return this instanceof Document || this instanceof Dataset || 
               this instanceof Model;
    }
    
    /**
     * Check if this type is an extension type.
     */
    default boolean isExtension() {
        return this instanceof Plugin || this instanceof Extension || 
               this instanceof Descriptor || this instanceof Manifest;
    }
    
    /**
     * Check if this type is a security type.
     */
    default boolean isSecurity() {
        return this instanceof User || this instanceof Role || 
               this instanceof Permission;
    }
    
    /**
     * Check if this type is an observability type.
     */
    default boolean isObservability() {
        return this instanceof Trace || this instanceof Metric || 
               this instanceof Log || this instanceof Audit;
    }
    
}