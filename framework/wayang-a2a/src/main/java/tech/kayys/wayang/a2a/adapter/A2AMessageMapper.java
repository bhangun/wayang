package tech.kayys.wayang.a2a.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tech.kayys.wayang.a2a.model.A2AArtifact;
import tech.kayys.wayang.a2a.model.A2AMessage;
import tech.kayys.wayang.a2a.model.A2APart;
import tech.kayys.wayang.a2a.model.A2ATask;
import tech.kayys.wayang.a2a.model.A2ATaskStatus;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.input.InputType;
import tech.kayys.wayang.resource.Artifact;

/**
 * Maps between A2A protocol models and Wayang internal semantic models.
 */
public class A2AMessageMapper {

    public static AgentRequest toAgentRequest(A2AMessage message) {
        StringBuilder contentBuilder = new StringBuilder();
        
        for (A2APart part : message.content()) {
            if (part instanceof A2APart.Text t) {
                contentBuilder.append(t.text()).append("\n");
            }
        }
        
        AgentRequest.AgentRequestBuilder builder = AgentRequest.builder()
            .id(UUID.randomUUID().toString())
            .type(InputType.TEXT)
            .content(contentBuilder.toString().trim())
            .userId(message.role() != null ? message.role().name() : "A2A");
            
        return builder.build();
    }
    
    public static A2ATask toCompletedTask(String taskId, AgentResponse response) {
        List<A2AArtifact> a2aArtifacts = new ArrayList<>();
        if (response.artifacts() != null) {
            for (Artifact art : response.artifacts()) {
                a2aArtifacts.add(new A2AArtifact(
                    art.id().asString(),
                    art.metadata() != null ? art.metadata().name() : "artifact",
                    art.type() != null ? art.type().toString() : "text",
                    art.asBytes() != null ? art.asBytes().length : 0L,
                    null,
                    art.metadata() != null ? Map.of("desc", art.metadata().description()) : Map.of()
                ));
            }
        }
        
        A2ATaskStatus status = response.success() ? A2ATaskStatus.COMPLETED : A2ATaskStatus.FAILED;
        
        Map<String, Object> taskMetadata = Map.of(
            "responseContent", response.content() != null ? response.content() : "",
            "error", response.error() != null ? response.error() : ""
        );
        
        return new A2ATask(
            taskId,
            response.id(),
            status,
            a2aArtifacts,
            taskMetadata
        );
    }
    
    public static A2ATask toFailedTask(String taskId, Throwable ex) {
        Map<String, Object> taskMetadata = Map.of("error", ex.getMessage() != null ? ex.getMessage() : ex.toString());
        return new A2ATask(
            taskId,
            null,
            A2ATaskStatus.FAILED,
            List.of(),
            taskMetadata
        );
    }
    
    public static A2ATask toInProgressTask(String taskId) {
        return new A2ATask(
            taskId,
            null,
            A2ATaskStatus.RUNNING,
            List.of(),
            Map.of()
        );
    }
}
