package tech.kayys.wayang.websearch.executor;

import io.smallrye.mutiny.Uni;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.kayys.gamelan.engine.node.*;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.wayang.node.websearch.SearchOrchestrator;
import tech.kayys.wayang.node.websearch.api.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
class WebSearchExecutorTest {

    @Mock
    private SearchOrchestrator searchOrchestrator;

    private WebSearchExecutor webSearchExecutor;

    private NodeExecutionTask createMockTask(Map<String, Object> context) {
        NodeId nodeId = new NodeId("test-node-" + UUID.randomUUID());
        WorkflowRunId runId = new WorkflowRunId("test-run-" + UUID.randomUUID());
        
        return new NodeExecutionTask(
                runId,
                nodeId,
                1,
                null, // token
                context,
                null // retry policy
        );
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webSearchExecutor = new WebSearchExecutor();
        webSearchExecutor.searchOrchestrator = searchOrchestrator;
    }

    @Test
    void testExecuteWithValidSearchRequest(VertxTestContext context) {
        // Arrange
        String query = "test query";
        Map<String, Object> taskContext = Map.of(
                "query", query,
                "searchType", "text",
                "maxResults", 5
        );
        NodeExecutionTask task = createMockTask(taskContext);

        // Mock search response
        List<SearchResult> mockResults = List.of(
                SearchResult.builder()
                        .title("Test Result 1")
                        .url("https://example.com/1")
                        .snippet("Test snippet 1")
                        .source("mock")
                        .score(100.0)
                        .build()
        );
        
        SearchResponse mockResponse = SearchResponse.builder()
                .results(mockResults)
                .totalResults(100)
                .providerUsed("mock-provider")
                .durationMs(100L)
                .build();

        when(searchOrchestrator.search(any(SearchRequest.class)))
                .thenReturn(Uni.createFrom().item(mockResponse));

        // Act & Assert
        webSearchExecutor.execute(task)
                .subscribe().with(
                        result -> {
                            try {
                                assertEquals(NodeExecutionStatus.COMPLETED, result.getStatus());
                                assertNotNull(result.output());
                                assertEquals(4, result.output().size()); // results, totalResults, providerUsed, durationMs
                                
                                List<?> results = (List<?>) result.output().get("results");
                                assertEquals(1, results.size());
                                
                                context.completeNow();
                            } catch (Throwable t) {
                                context.failNow(t);
                            }
                        },
                        context::failNow
                );
    }

    @Test
    void testExecuteWithMissingQuery(VertxTestContext context) {
        // Arrange
        Map<String, Object> taskContext = Map.of(); // No query
        NodeExecutionTask task = createMockTask(taskContext);

        // Act & Assert
        webSearchExecutor.execute(task)
                .subscribe().with(
                        result -> {
                            try {
                                assertEquals(NodeExecutionStatus.FAILED, result.getStatus());
                                assertNotNull(result.error());
                                assertEquals("WEB_SEARCH_ERROR", result.error().code());
                                assertTrue(result.error().message().contains("Missing search query"));
                                
                                context.completeNow();
                            } catch (Throwable t) {
                                context.failNow(t);
                            }
                        },
                        context::failNow
                );
    }

    @Test
    void testExecuteWithEmptyQuery(VertxTestContext context) {
        // Arrange
        Map<String, Object> taskContext = Map.of("query", "");
        NodeExecutionTask task = createMockTask(taskContext);

        // Act & Assert
        webSearchExecutor.execute(task)
                .subscribe().with(
                        result -> {
                            try {
                                assertEquals(NodeExecutionStatus.FAILED, result.getStatus());
                                assertNotNull(result.error());
                                assertEquals("WEB_SEARCH_ERROR", result.error().code());
                                assertTrue(result.error().message().contains("Missing search query"));
                                
                                context.completeNow();
                            } catch (Throwable t) {
                                context.failNow(t);
                            }
                        },
                        context::failNow
                );
    }

    @Test
    void testExecuteWithShortQuery(VertxTestContext context) {
        // Arrange
        Map<String, Object> taskContext = Map.of("query", "a"); // Too short
        NodeExecutionTask task = createMockTask(taskContext);

        // Act & Assert
        webSearchExecutor.execute(task)
                .subscribe().with(
                        result -> {
                            try {
                                assertEquals(NodeExecutionStatus.FAILED, result.getStatus());
                                assertNotNull(result.error());
                                assertEquals("WEB_SEARCH_ERROR", result.error().code());
                                assertTrue(result.error().message().contains("at least 2 characters"));
                                
                                context.completeNow();
                            } catch (Throwable t) {
                                context.failNow(t);
                            }
                        },
                        context::failNow
                );
    }

    @Test
    void testExecuteWithSearchFailure(VertxTestContext context) {
        // Arrange
        String query = "test query";
        Map<String, Object> taskContext = Map.of("query", query);
        NodeExecutionTask task = createMockTask(taskContext);

        when(searchOrchestrator.search(any(SearchRequest.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Search service unavailable")));

        // Act & Assert
        webSearchExecutor.execute(task)
                .subscribe().with(
                        result -> {
                            try {
                                assertEquals(NodeExecutionStatus.FAILED, result.getStatus());
                                assertNotNull(result.error());
                                assertEquals("WEB_SEARCH_ERROR", result.error().code());
                                assertTrue(result.error().message().contains("Search service unavailable"));
                                
                                context.completeNow();
                            } catch (Throwable t) {
                                context.failNow(t);
                            }
                        },
                        context::failNow
                );
    }

    @Test
    void testExecuteParsesStringMaxResultsAndTrimsQuery(VertxTestContext context) {
        Map<String, Object> taskContext = Map.of(
                "query", "   test query   ",
                "searchType", "TEXT",
                "maxResults", "7"
        );
        NodeExecutionTask task = createMockTask(taskContext);

        SearchResponse mockResponse = SearchResponse.builder()
                .results(List.of())
                .totalResults(0)
                .providerUsed("mock-provider")
                .durationMs(50L)
                .build();

        when(searchOrchestrator.search(any(SearchRequest.class)))
                .thenReturn(Uni.createFrom().item(mockResponse));

        webSearchExecutor.execute(task).subscribe().with(
                result -> {
                    try {
                        assertEquals(NodeExecutionStatus.COMPLETED, result.getStatus());

                        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
                        verify(searchOrchestrator).search(captor.capture());
                        SearchRequest sentRequest = captor.getValue();
                        assertEquals("test query", sentRequest.query());
                        assertEquals("text", sentRequest.searchType());
                        assertEquals(7, sentRequest.maxResults());

                        context.completeNow();
                    } catch (Throwable t) {
                        context.failNow(t);
                    }
                },
                context::failNow
        );
    }

    @Test
    void testExecuteDefaultsInvalidMaxResultsString(VertxTestContext context) {
        Map<String, Object> taskContext = Map.of(
                "query", "test query",
                "maxResults", "abc"
        );
        NodeExecutionTask task = createMockTask(taskContext);

        SearchResponse mockResponse = SearchResponse.builder()
                .results(List.of())
                .totalResults(0)
                .providerUsed("mock-provider")
                .durationMs(50L)
                .build();

        when(searchOrchestrator.search(any(SearchRequest.class)))
                .thenReturn(Uni.createFrom().item(mockResponse));

        webSearchExecutor.execute(task).subscribe().with(
                result -> {
                    try {
                        assertEquals(NodeExecutionStatus.COMPLETED, result.getStatus());

                        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
                        verify(searchOrchestrator).search(captor.capture());
                        assertEquals(10, captor.getValue().maxResults());

                        context.completeNow();
                    } catch (Throwable t) {
                        context.failNow(t);
                    }
                },
                context::failNow
        );
    }

    @Test
    void testGetExecutorType() {
        // Act
        String executorType = webSearchExecutor.getExecutorType();

        // Assert
        assertEquals("web-search", executorType);
    }

    @Test
    void testGetSupportedNodeTypes() {
        // Act
        String[] supportedTypes = webSearchExecutor.getSupportedNodeTypes();

        // Assert
        assertArrayEquals(new String[]{"web-search", "search", "web-search-node"}, supportedTypes);
    }

    @Test
    void testExtractSearchQueryFromDifferentKeys() {
        // Test different possible query keys
        Map<String, Object> context1 = Map.of("query", "test query 1");
        Map<String, Object> context2 = Map.of("searchQuery", "test query 2");
        Map<String, Object> context3 = Map.of("input", "test query 3");
        Map<String, Object> context4 = Map.of("inputData", Map.of("query", "test query 4"));

        // Using reflection to access the private method
        try {
            java.lang.reflect.Method extractMethod = WebSearchExecutor.class
                    .getDeclaredMethod("extractSearchQuery", Map.class);
            extractMethod.setAccessible(true);

            String result1 = (String) extractMethod.invoke(webSearchExecutor, context1);
            String result2 = (String) extractMethod.invoke(webSearchExecutor, context2);
            String result3 = (String) extractMethod.invoke(webSearchExecutor, context3);
            String result4 = (String) extractMethod.invoke(webSearchExecutor, context4);

            assertEquals("test query 1", result1);
            assertEquals("test query 2", result2);
            assertEquals("test query 3", result3);
            assertEquals("test query 4", result4);

        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}
