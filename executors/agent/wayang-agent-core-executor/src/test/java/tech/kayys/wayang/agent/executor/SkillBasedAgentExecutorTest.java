package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.agent.core.tool.ToolRegistry;
import tech.kayys.wayang.agent.skill.SkillDefinition;
import tech.kayys.wayang.agent.skill.SkillPromptRenderer;
import tech.kayys.wayang.agent.skill.SkillRegistry;
import tech.kayys.wayang.tool.spi.Tool;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class SkillBasedAgentExecutorTest {

    @InjectMocks
    SkillBasedAgentExecutor executor;

    @Mock
    SkillRegistry skillRegistry;

    @Mock
    SkillPromptRenderer promptRenderer;

    @Mock
    GollekInferenceService inferenceService;

    @Mock
    ToolRegistry toolRegistry;

    @BeforeEach
    void setUp() {
        // Mock prompt rendering to succeed
        Mockito.lenient().when(promptRenderer.renderSystemPrompt(any(), any()))
                .thenReturn("System Prompt: You are a helpful bot.");
        Mockito.lenient().when(promptRenderer.renderUserPrompt(any(), any(), any()))
                .thenReturn("User Input: Hello test");
    }

    @Test
    void testExecuteResolvesSkillAndCorrectlyIntegratesToolsAndModels() {
        // Setup a mock task context
        NodeExecutionTask task = Mockito.mock(NodeExecutionTask.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.lenient().when(task.runId().value()).thenReturn("runId");
        Mockito.lenient().when(task.nodeId().value()).thenReturn("nodeId");
        Map<String, Object> context = Map.of(
                "skillId", "test-skill-coder",
                "instruction", "Write a test",
                "taskType", "GENERATE"
        );
        Mockito.when(task.context()).thenReturn(context);

        // Mock skill definition retrieved from database/JSON
        SkillDefinition skillDefinition = SkillDefinition.builder()
                .id("test-skill-coder")
                .name("Coder Skill")
                .systemPrompt("Test System Prompt")
                .defaultProvider("test-cloud-provider")
                .temperature(0.5)
                .maxTokens(2000)
                .tools(List.of("search-tool", "calculator-tool"))
                .build();
        Mockito.when(skillRegistry.getSkill("test-skill-coder")).thenReturn(Optional.of(skillDefinition));

        // Mock tools
        Tool searchTool = Mockito.mock(Tool.class);
        Mockito.when(searchTool.id()).thenReturn("search-tool");
        Mockito.when(searchTool.description()).thenReturn("Web Search");
        Mockito.when(searchTool.inputSchema()).thenReturn(Map.of("type", "object"));

        Mockito.when(toolRegistry.getTool("search-tool")).thenReturn(Optional.of(searchTool));
        Mockito.when(toolRegistry.getTool("calculator-tool")).thenReturn(Optional.empty()); // One tool missing

        // Mock inference response
        AgentInferenceResponse mockResponse = AgentInferenceResponse.builder()
                .content("Result code snippet")
                .providerUsed("test-cloud-provider")
                .promptTokens(10)
                .completionTokens(20)
                .build();
        Mockito.when(inferenceService.inferWithToolLoop(any()))
                .thenReturn(mockResponse);

        // Execute
        NodeExecutionResult result = executor.execute(task)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Verify the execution completed with expected text
        Assertions.assertEquals(tech.kayys.gamelan.engine.node.NodeExecutionStatus.COMPLETED, result.status());
        Assertions.assertTrue(result.output().get("result").toString().contains("Result code snippet"));

        // Verify Inference Request captures correct models and tools
        ArgumentCaptor<AgentInferenceRequest> captor = ArgumentCaptor.forClass(AgentInferenceRequest.class);
        Mockito.verify(inferenceService).inferWithToolLoop(captor.capture());
        
        AgentInferenceRequest req = captor.getValue();
        Assertions.assertEquals("System Prompt: You are a helpful bot.", req.getSystemPrompt());
        Assertions.assertEquals("User Input: Hello test", req.getUserPrompt());
        Assertions.assertEquals(0.5, req.getTemperature());
        Assertions.assertEquals(2000, req.getMaxTokens());
        Assertions.assertEquals("test-cloud-provider", req.getPreferredProvider());

        // Verify Tools mapping logic
        Assertions.assertNotNull(req.getTools());
        Assertions.assertEquals(1, req.getTools().size(), "Only found 1 valid tool out of the 2 specified");
        Assertions.assertEquals("search-tool", req.getTools().get(0).getName());
        Assertions.assertEquals("Web Search", req.getTools().get(0).getDescription().orElse(""));
    }

    @Test
    void testExecuteFailsWhenSkillNotFound() {
        NodeExecutionTask task = Mockito.mock(NodeExecutionTask.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.lenient().when(task.runId().value()).thenReturn("runId");
        Mockito.lenient().when(task.nodeId().value()).thenReturn("nodeId");
        Map<String, Object> context = Map.of("skillId", "non-existent-skill");
        Mockito.when(task.context()).thenReturn(context);

        // Registry returns empty
        Mockito.when(skillRegistry.getSkill("non-existent-skill")).thenReturn(Optional.empty());

        // We also want to mock the fallback `common` skill failing to ensure failure bubbles up
        Mockito.lenient().when(skillRegistry.getSkill("common")).thenReturn(Optional.empty());

        NodeExecutionResult result = executor.execute(task)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        Assertions.assertEquals(tech.kayys.gamelan.engine.node.NodeExecutionStatus.FAILED, result.status());
    }
}
