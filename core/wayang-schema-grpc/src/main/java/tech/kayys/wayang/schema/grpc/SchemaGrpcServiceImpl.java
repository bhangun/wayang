package tech.kayys.wayang.schema.grpc;

import io.grpc.stub.StreamObserver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.schema.validator.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class SchemaGrpcServiceImpl extends SchemaServiceGrpc.SchemaServiceImplBase {

    @Inject
    SchemaValidationService schemaValidationService;

    @Inject
    AgentConfigValidator agentConfigValidator;

    @Inject
    WorkflowValidator workflowValidator;

    @Inject
    PluginConfigValidator pluginConfigValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void validateSchema(SchemaValidationRequest request, StreamObserver<SchemaValidationResponse> responseObserver) {
        try {
            // Convert JSON strings to Maps
            java.util.Map<String, Object> dataMap = objectMapper.readValue(request.getData(), java.util.Map.class);
            
            // Perform validation
            ValidationResult result = schemaValidationService.validateSchema(request.getSchema(), dataMap);
            
            // Build response
            SchemaValidationResponse.Builder responseBuilder = SchemaValidationResponse.newBuilder()
                    .setValid(result.isValid());
            
            if (!result.isValid()) {
                responseBuilder.setMessage(result.getMessage());
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void validateAgentConfig(AgentConfigValidationRequest request, StreamObserver<ValidationResponse> responseObserver) {
        try {
            // Convert JSON string to Map
            java.util.Map<String, Object> configMap = objectMapper.readValue(request.getAgentConfig(), java.util.Map.class);
            
            // Perform validation
            ValidationResult result = agentConfigValidator.validateAgentConfig(configMap);
            
            // Build response
            ValidationResponse.Builder responseBuilder = ValidationResponse.newBuilder()
                    .setValid(result.isValid());
            
            if (!result.isValid()) {
                responseBuilder.setMessage(result.getMessage());
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void validateWorkflow(WorkflowValidationRequest request, StreamObserver<ValidationResponse> responseObserver) {
        try {
            // Convert JSON string to Map
            java.util.Map<String, Object> workflowMap = objectMapper.readValue(request.getWorkflow(), java.util.Map.class);
            
            // Perform validation
            ValidationResult result = workflowValidator.validateWorkflow(workflowMap);
            
            // Build response
            ValidationResponse.Builder responseBuilder = ValidationResponse.newBuilder()
                    .setValid(result.isValid());
            
            if (!result.isValid()) {
                responseBuilder.setMessage(result.getMessage());
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void validatePluginConfig(PluginConfigValidationRequest request, StreamObserver<ValidationResponse> responseObserver) {
        try {
            // Convert JSON string to Map
            java.util.Map<String, Object> configMap = objectMapper.readValue(request.getPluginConfig(), java.util.Map.class);
            
            // Perform validation
            ValidationResult result = pluginConfigValidator.validatePluginConfig(configMap);
            
            // Build response
            ValidationResponse.Builder responseBuilder = ValidationResponse.newBuilder()
                    .setValid(result.isValid());
            
            if (!result.isValid()) {
                responseBuilder.setMessage(result.getMessage());
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void validateWithRules(SchemaValidationRequest request, StreamObserver<SchemaValidationResponse> responseObserver) {
        // This would require a more complex implementation to handle rules passed as part of the request
        // For now, we'll implement basic schema validation
        validateSchema(request, responseObserver);
    }
}