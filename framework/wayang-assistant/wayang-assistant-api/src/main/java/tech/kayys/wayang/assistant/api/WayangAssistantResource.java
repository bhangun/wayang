package tech.kayys.wayang.assistant.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import tech.kayys.wayang.assistant.agent.WayangAssistantService;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.*;
import tech.kayys.wayang.project.api.ProjectDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple REST resource exposing the internal assistant operations.
 */
@ApplicationScoped
@Path("/assistant")
public class WayangAssistantResource {

    @Inject
    WayangAssistantService assistantService;

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> search(Map<String, String> body) {
        String query = body.get("query");
        List<DocSearchResult> results = assistantService.searchDocumentation(query);
        
        List<Map<String, Object>> resultMaps = new java.util.ArrayList<>();
        for (DocSearchResult r : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("title", r.getTitle());
            item.put("snippet", r.getSnippet());
            item.put("url", r.getUrl());
            item.put("score", r.getScore());
            resultMaps.add(item);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("results", resultMaps);
        response.put("resultCount", results.size());
        return response;
    }

    @POST
    @Path("/generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> generate(Map<String, String> body) {
        String intent = body.get("intent");
        ProjectDescriptor project = assistantService.generateProject(intent);
        Map<String, Object> response = new HashMap<>();
        response.put("project", project);
        return response;
    }

    @POST
    @Path("/troubleshoot")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> troubleshoot(Map<String, String> body) {
        String err = body.get("error");
        ErrorTroubleshootingResult result = assistantService.troubleshootError(err);
        
        Map<String, Object> response = new HashMap<>();
        response.put("errorMessage", result.getErrorMessage());
        response.put("advice", result.getAdvice());
        response.put("resultCount", result.getDocumentationResults().size());
        
        return response;
    }
}
