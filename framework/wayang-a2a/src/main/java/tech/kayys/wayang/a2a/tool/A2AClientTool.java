package tech.kayys.wayang.a2a.tool;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.a2a.api.A2AClient;
import tech.kayys.wayang.a2a.model.A2AMessage;
import tech.kayys.wayang.a2a.model.A2APart;
import tech.kayys.wayang.descriptor.CapabilityDescriptor;
import tech.kayys.wayang.descriptor.ParameterDescriptor;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolDescriptor;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

public class A2AClientTool implements Tool {
    private final ResourceId toolId;
    private final A2AClient a2aClient;
    private final String targetAgentId;

    public A2AClientTool(A2AClient a2aClient, String targetAgentId) {
        this.toolId = new ResourceId.ToolId(Id.random());
        this.a2aClient = a2aClient;
        this.targetAgentId = targetAgentId;
    }

    @Override
    public ResourceId id() {
        return toolId;
    }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Tool();
    }
    
    @Override
    public Metadata metadata() {
        return Metadata.empty();
    }

    @Override
    public ToolDescriptor descriptor() {
        return new ToolDescriptor() {
            @Override
            public ResourceId id() {
                return toolId;
            }
            
            @Override
            public ResourceType type() {
                return new ResourceType.Custom("descriptor");
            }

            @Override
            public String name() {
                return "SendMessageToAgent";
            }

            @Override
            public String description() {
                return "Send a message to external agent: " + targetAgentId;
            }

            @Override
            public String version() {
                return "1.0.0";
            }
            
            @Override
            public Metadata metadata() {
                return Metadata.empty();
            }
            
            @Override
            public java.util.Set<String> tags() {
                return java.util.Set.of("a2a", "communication");
            }
            
            @Override
            public java.util.Set<String> categories() {
                return java.util.Set.of("network");
            }
            
            @Override
            public java.util.List<CapabilityDescriptor> capabilities() {
                return java.util.List.of();
            }
            
            @Override
            public Map<String, ParameterDescriptor> inputs() {
                return Map.of();
            }
            
            @Override
            public Map<String, ParameterDescriptor> outputs() {
                return Map.of();
            }

            @Override
            public Map<String, Object> inputSchema() {
                return Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "message", Map.of(
                            "type", "string",
                            "description", "The message text to send to the external agent"
                        )
                    ),
                    "required", java.util.List.of("message")
                );
            }
        };
    }

    @Override
    public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
        String content = (String) invocation.arguments().get("message");
        if (content == null) {
            content = "";
        }
        
        A2AMessage message = new A2AMessage(
            A2AMessage.Role.USER,
            java.util.List.of(new A2APart.Text(content))
        );

        return a2aClient.sendMessage(message).handle((task, ex) -> {
            if (ex != null) {
                return createFailedResult(ex.getMessage());
            }
            return createSuccessResult(task.taskId(), task.status().name());
        }).toCompletableFuture();
    }

    private ToolResult createSuccessResult(String taskId, String status) {
        return new ToolResult() {
            @Override
            public ResourceId id() {
                return new ResourceId.CustomId(Id.random(), new ResourceType.Custom("toolResult"));
            }
            
            @Override
            public ResourceType type() {
                return new ResourceType.Custom("toolResult");
            }
            
            @Override
            public Metadata metadata() {
                return Metadata.empty();
            }

            @Override
            public Map<String, Object> getOutputs() {
                return Map.of(
                    "taskId", taskId,
                    "status", status
                );
            }

            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getErrorMessage() {
                return null;
            }
        };
    }

    private ToolResult createFailedResult(String error) {
        return new ToolResult() {
            @Override
            public ResourceId id() {
                return new ResourceId.CustomId(Id.random(), new ResourceType.Custom("toolResult"));
            }
            
            @Override
            public ResourceType type() {
                return new ResourceType.Custom("toolResult");
            }
            
            @Override
            public Metadata metadata() {
                return Metadata.empty();
            }

            @Override
            public Map<String, Object> getOutputs() {
                return Map.of();
            }

            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public String getErrorMessage() {
                return error;
            }
        };
    }
}
