package tech.kayys.wayang.identity;
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

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Strongly typed identifiers for different resource types.
 * Prevents mixing IDs of different types.
 */
public interface ResourceId {
    
    Id value();
    
    ResourceType type();
    
    default String asString() {
        return value().asString();
    }
    
    record AgentId(Id value) implements ResourceId {
        public AgentId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Agent(); }
    }
    
    record SkillId(Id value) implements ResourceId {
        public SkillId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Skill(); }
    }
    
    record ToolId(Id value) implements ResourceId {
        public ToolId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Tool(); }
    }
    
    record WorkflowId(Id value) implements ResourceId {
        public WorkflowId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Workflow(); }
    }
    
    record PromptId(Id value) implements ResourceId {
        public PromptId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Prompt(); }
    }
    
    record PolicyId(Id value) implements ResourceId {
        public PolicyId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Policy(); }
    }
    
    record ExecutionId(Id value) implements ResourceId {
        public ExecutionId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Execution(); }
    }
    
    record SessionId(Id value) implements ResourceId {
        public SessionId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Session(); }
    }
    
    record ArtifactId(Id value) implements ResourceId {
        public ArtifactId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Artifact(); }
    }
    
    record EventId(Id value) implements ResourceId {
        public EventId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Event(); }
    }
    
    record PluginId(Id value) implements ResourceId {
        public PluginId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Plugin(); }
    }
    
    record DocumentId(Id value) implements ResourceId {
        public DocumentId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Document(); }
    }
    
    record ModelId(Id value) implements ResourceId {
        public ModelId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Model(); }
    }
    
    // Correlation and causation IDs
    record CorrelationId(Id value) implements ResourceId {
        public CorrelationId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Custom("correlation"); }
    }
    
    record CausationId(Id value) implements ResourceId {
        public CausationId {
            Objects.requireNonNull(value, "value cannot be null");
        }
        @Override
        public ResourceType type() { return new ResourceType.Custom("causation"); }
    }
    
    // Custom ID for unknown types
    record CustomId(Id value, ResourceType type) implements ResourceId {
        public CustomId {
            Objects.requireNonNull(value, "value cannot be null");
            Objects.requireNonNull(type, "type cannot be null");
        }
        @Override
        public ResourceType type() { return type; }
    }
    
    static ResourceId from(Id value, ResourceType type) {
        return switch (type) {
            case ResourceType.Agent ignored -> new AgentId(value);
            case ResourceType.Skill ignored -> new SkillId(value);
            case ResourceType.Tool ignored -> new ToolId(value);
            case ResourceType.Workflow ignored -> new WorkflowId(value);
            case ResourceType.Prompt ignored -> new PromptId(value);
            case ResourceType.Policy ignored -> new PolicyId(value);
            case ResourceType.Execution ignored -> new ExecutionId(value);
            case ResourceType.Session ignored -> new SessionId(value);
            case ResourceType.Artifact ignored -> new ArtifactId(value);
            case ResourceType.Event ignored -> new EventId(value);
            case ResourceType.Plugin ignored -> new PluginId(value);
            case ResourceType.Document ignored -> new DocumentId(value);
            case ResourceType.Model ignored -> new ModelId(value);
            default -> new CustomId(value, type);
        };
    }
}