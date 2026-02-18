package tech.kayys.wayang.agent.core.inference;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.kayys.gollek.sdk.local.GollekLocalClient;
import tech.kayys.gollek.spi.inference.InferenceRequest;
import tech.kayys.gollek.spi.inference.InferenceResponse;
import tech.kayys.wayang.agent.core.memory.AgentMemoryService;
import tech.kayys.wayang.agent.core.tool.ToolRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class GollekInferenceServiceTest {

        @InjectMocks
        GollekInferenceService inferenceService;

        @Mock
        GollekLocalClient gollekClient;

        @Mock
        AgentMemoryService memoryService;

        @Mock
        ToolRegistry toolRegistry;

        @Test
        void testInferBasic() {
                InferenceResponse mockResponse = InferenceResponse.builder()
                                .requestId("test-req-1")
                                .content("response content")
                                .model("model-id")
                                .build();
                Mockito.when(gollekClient.createCompletion(any(InferenceRequest.class))).thenReturn(mockResponse);

                AgentInferenceRequest request = AgentInferenceRequest.builder()
                                .userPrompt("hello")
                                .model("model-id")
                                .build();

                AgentInferenceResponse response = inferenceService.infer(request);

                Assertions.assertEquals("response content", response.getContent());
                Assertions.assertNull(response.getProviderUsed());
        }

        @Test
        void testInferWithMemory() {
                InferenceResponse mockResponse = InferenceResponse.builder()
                                .requestId("test-req-2")
                                .content("response content")
                                .model("model-id")
                                .build();
                Mockito.when(gollekClient.createCompletion(any(InferenceRequest.class))).thenReturn(mockResponse);
                Mockito.when(memoryService.retrieveContext(anyString(), anyString(), anyInt()))
                                .thenReturn(Uni.createFrom().item("Previous context"));
                Mockito.when(memoryService.storeMemory(anyString(), anyString(), any()))
                                .thenReturn(Uni.createFrom().item("mem-id"));

                AgentInferenceRequest request = AgentInferenceRequest.builder()
                                .userPrompt("hello")
                                .model("model-id")
                                .useMemory(true)
                                .agentId("agent-1")
                                .build();

                AgentInferenceResponse response = inferenceService.infer(request);

                Assertions.assertEquals("response content", response.getContent());
                Mockito.verify(memoryService).retrieveContext(eq("agent-1"), eq("hello"), anyInt());
                Mockito.verify(gollekClient).createCompletion(any(InferenceRequest.class));
        }
}
