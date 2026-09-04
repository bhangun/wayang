package tech.kayys.wayang.memory.visual;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.memory.context.*;
import tech.kayys.wayang.memory.model.ConversationMemory;
import tech.kayys.wayang.memory.model.Memory;
import tech.kayys.wayang.memory.model.MemoryType;
import tech.kayys.wayang.memory.service.InMemoryVectorStore;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMemoryVisualProjectionServiceTest {

    private DefaultMemoryVisualProjectionService service;
    private InMemoryVectorStore vectorStore;

    @BeforeEach
    void setUp() {
        vectorStore = new InMemoryVectorStore();
        service = new DefaultMemoryVisualProjectionService(new HierarchicalMemoryManager(), vectorStore);
    }

    @Test
    void testOverviewProjection() {
        Memory mem1 = Memory.builder()
                .content("User prefers concise Kotlin code")
                .type(MemoryType.SEMANTIC)
                .metadata(Map.of("category", "Preferences"))
                .build();
        vectorStore.store(mem1).await().indefinitely();

        MemoryVisualQuery query = MemoryVisualQuery.forAgent("agent-01");
        MemoryVisualView overview = service.overview(query);

        assertThat(overview).isNotNull();
        assertThat(overview.viewType()).isEqualTo(MemoryVisualView.MemoryVisualViewType.OVERVIEW);
        assertThat(overview.agentId()).isEqualTo("agent-01");
        assertThat(overview.hierarchy().longTermTotalRecords()).isEqualTo(1L);
        assertThat(overview.hierarchy().categoryDistribution()).containsEntry("Preferences", 1L);
    }

    @Test
    void testSemanticGraphProjection() {
        SemanticMemory semantic = new SemanticMemory(
                Map.of("Quarkus", List.of("Reactive", "gRPC")),
                List.of(new Fact("Wayang", "orchestrates", "Agents", 0.95, Instant.now())),
                Map.of("Quarkus", 0.9),
                Instant.now()
        );
        service.registerSemanticMemory("agent-01", semantic);

        MemoryVisualQuery query = MemoryVisualQuery.forAgent("agent-01");
        MemoryVisualView graph = service.graph(query);

        assertThat(graph).isNotNull();
        assertThat(graph.viewType()).isEqualTo(MemoryVisualView.MemoryVisualViewType.GRAPH);
        assertThat(graph.nodes()).isNotEmpty();
        assertThat(graph.edges()).isNotEmpty();
        assertThat(graph.nodes()).anyMatch(n -> n.id().equals("Quarkus"));
        assertThat(graph.nodes()).anyMatch(n -> n.id().equals("Wayang"));
    }

    @Test
    void testVectorsProjection3D() {
        Memory mem = Memory.builder()
                .content("High-performance distributed execution engine")
                .type(MemoryType.SEMANTIC)
                .embedding(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f})
                .metadata(Map.of("category", "Architecture"))
                .importance(0.92)
                .build();
        vectorStore.store(mem).await().indefinitely();

        MemoryVisualQuery query3D = new MemoryVisualQuery("agent-01", null, "default", "default", null, null, 100, 3);
        MemoryVisualView vectors3D = service.vectors(query3D);

        assertThat(vectors3D).isNotNull();
        assertThat(vectors3D.viewType()).isEqualTo(MemoryVisualView.MemoryVisualViewType.VECTORS);
        assertThat(vectors3D.vectorPoints()).hasSize(1);
        MemoryVectorPoint p3D = vectors3D.vectorPoints().get(0);
        assertThat(p3D.category()).isEqualTo("Architecture");
        assertThat(p3D.z()).isNotZero();

        MemoryVisualQuery query2D = new MemoryVisualQuery("agent-01", null, "default", "default", null, null, 100, 2);
        MemoryVisualView vectors2D = service.vectors(query2D);
        MemoryVectorPoint p2D = vectors2D.vectorPoints().get(0);
        assertThat(p2D.z()).isEqualTo(0.0);
    }

    @Test
    void testWorkingMemoryProjection() {
        WorkingMemory working = new WorkingMemory(
                "Optimize memory visualization",
                List.of(),
                List.of(new Fact("Model", "is", "Gemini", 0.99, Instant.now())),
                List.of(new TaskPattern("CODING", 5, List.of("Plan", "Code", "Test"), 0.92)),
                Map.of("fact_0", 0.95),
                Instant.now()
        );
        service.registerWorkingMemory("session-123", working);

        MemoryVisualQuery query = MemoryVisualQuery.forSession("session-123");
        MemoryVisualView workingView = service.working(query);

        assertThat(workingView).isNotNull();
        assertThat(workingView.viewType()).isEqualTo(MemoryVisualView.MemoryVisualViewType.WORKING);
        assertThat(workingView.workingState().currentTask()).isEqualTo("Optimize memory visualization");
        assertThat(workingView.workingState().activeFacts()).hasSize(1);
        assertThat(workingView.workingState().activePatterns()).contains("CODING");
    }

    @Test
    void testProceduralMemoryProjection() {
        ProceduralMemory procedural = new ProceduralMemory(
                "user-01",
                List.of(new TaskPattern("REFACTOR", 3, List.of("Analyze", "Edit", "Verify"), 0.95)),
                Map.of("Analyze", List.of("Edit")),
                Map.of("Java", 0.88),
                Instant.now()
        );
        service.registerProceduralMemory("user-01", procedural);

        MemoryVisualQuery query = MemoryVisualQuery.forUser("user-01");
        MemoryVisualView proceduralView = service.procedural(query);

        assertThat(proceduralView).isNotNull();
        assertThat(proceduralView.viewType()).isEqualTo(MemoryVisualView.MemoryVisualViewType.PROCEDURAL);
        assertThat(proceduralView.nodes()).anyMatch(n -> n.id().equals("REFACTOR"));
        assertThat(proceduralView.nodes()).anyMatch(n -> n.id().equals("skill-Java"));
    }

    @Test
    void testFullComposedProjection() {
        MemoryVisualQuery query = MemoryVisualQuery.forAgent("agent-01");
        MemoryVisualView fullView = service.full(query);

        assertThat(fullView).isNotNull();
        assertThat(fullView.viewType()).isEqualTo(MemoryVisualView.MemoryVisualViewType.FULL);
        assertThat(fullView.hierarchy()).isNotNull();
        assertThat(fullView.workingState()).isNotNull();
    }
}
