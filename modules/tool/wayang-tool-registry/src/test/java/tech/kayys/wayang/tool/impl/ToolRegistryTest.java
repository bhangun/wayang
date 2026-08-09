package tech.kayys.wayang.tool.impl;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolDescriptor;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolProvider;
import tech.kayys.wayang.tool.ToolResult;
import tech.kayys.wayang.tool.capability.Capability;
import tech.kayys.wayang.tool.capability.CapabilityRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToolRegistryTest {

    @Test
    public void testCapabilityFiltering() {
        InMemoryToolRegistry registry = new InMemoryToolRegistry();

        // A mock provider that supplies one tool with a specific capability
        ToolProvider mockProvider = () -> List.of(new Tool() {
            @Override
            public ToolDescriptor descriptor() {
                return new ToolDescriptor() {
                    public String name() { return "mock-tool"; }
                    public String description() { return "mock"; }
                    public String version() { return "1"; }
                };
            }

            @Override
            public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
                return null;
            }

            @Override
            public Collection<Capability> capabilities() {
                return List.of(new Capability() {
                    @Override
                    public String id() { return "math"; }
                    
                    @Override
                    public boolean satisfies(Capability requested) { return true; }
                });
            }
            
            @Override
            public String id() { return "mock-tool"; }
            @Override
            public void start() throws Exception {}
            @Override
            public void stop() throws Exception {}
        });

        registry.registerProvider(mockProvider);

        // Request tools with math capability
        CapabilityRequest request = new CapabilityRequest().require(new Capability() {
            @Override public String id() { return "math"; }
            @Override public boolean satisfies(Capability requested) { return true; }
        });

        List<Tool> found = registry.getToolsByCapability(request);
        assertEquals(1, found.size());
        assertEquals("mock-tool", found.get(0).descriptor().name());
        
        // Request tools with unknown capability
        CapabilityRequest unknownRequest = new CapabilityRequest().require(new Capability() {
            @Override public String id() { return "unknown"; }
            @Override public boolean satisfies(Capability requested) { return true; }
        });
        
        List<Tool> empty = registry.getToolsByCapability(unknownRequest);
        assertEquals(0, empty.size());
    }
}
