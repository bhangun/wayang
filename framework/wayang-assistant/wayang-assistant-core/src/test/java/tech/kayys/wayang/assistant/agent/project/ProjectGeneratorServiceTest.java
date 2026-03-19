package tech.kayys.wayang.assistant.agent.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.project.api.ProjectDescriptor;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectGeneratorServiceTest {

    private ProjectGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new ProjectGeneratorService();
    }

    @Test
    void generateProject_ragIntent() {
        ProjectDescriptor desc = service.generateProject("Build a RAG bot");
        assertNotNull(desc);
        assertTrue(desc.getCapabilities().contains("rag"));
        assertNotNull(desc.getName());
    }

    @Test
    void generateProject_multiAgentIntent() {
        ProjectDescriptor desc = service.generateProject("Create a multi-agent system");
        assertNotNull(desc);
        assertTrue(desc.getCapabilities().contains("orchestrator"));
    }
}
