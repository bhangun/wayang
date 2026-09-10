package tech.kayys.wayang.agent.orchestration;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.descriptor.CapabilityDescriptor;
import tech.kayys.wayang.descriptor.ParameterDescriptor;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolDescriptor;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class ToolDefinitionMapperTest {

    @Test
    void mapsCanonicalToolToAgentToolDefinition() {
        ToolSpec definition = ToolDefinitionMapper.fromTool(new EchoTool());

        assertThat(definition.name()).isEqualTo("Echo");
        assertThat(definition.description()).isEqualTo("Echo input text");
        assertThat(definition.inputSchema()).containsEntry("type", "object");
    }

    private static final class EchoTool implements Tool {
        private final ResourceId toolId = new ResourceId.ToolId(Id.random());

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
                public Metadata metadata() {
                    return Metadata.empty();
                }

                @Override
                public Set<String> tags() {
                    return Set.of();
                }

                @Override
                public Set<String> categories() {
                    return Set.of();
                }

                @Override
                public List<CapabilityDescriptor> capabilities() {
                    return List.of();
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
                public String name() {
                    return "Echo";
                }

                @Override
                public String description() {
                    return "Echo input text";
                }

                @Override
                public String version() {
                    return "1.0.0";
                }

                @Override
                public Map<String, Object> inputSchema() {
                    return Map.of("type", "object");
                }
            };
        }

        @Override
        public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
            return CompletableFuture.completedFuture(new ToolResult() {
                @Override
                public ResourceId id() {
                    return toolId;
                }

                @Override
                public ResourceType type() {
                    return new ResourceType.Custom("result");
                }

                @Override
                public Metadata metadata() {
                    return Metadata.empty();
                }

                @Override
                public Map<String, Object> getOutputs() {
                    return Map.of("echo", "output");
                }

                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getErrorMessage() {
                    return null;
                }
            });
        }
    }
}
