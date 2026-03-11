/*
 * PolyForm Noncommercial License 1.0.0
 *
 * Copyright (c) 2026 Kayys.tech
 */

package tech.kayys.wayang.control.api;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.kayys.wayang.control.dto.AgentTool;
import tech.kayys.wayang.control.dto.ToolType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API for managing and discovery of AI Agent tools.
 */
@Path("/api/v1/tools")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ToolResource {

    @ConfigProperty(name = "wayang.agent.tools.web-search.enabled", defaultValue = "true")
    boolean webSearchEnabled;

    @GET
    @Path("/builtin")
    public Uni<Response> listBuiltinTools() {
        List<AgentTool> tools = new ArrayList<>();

        if (webSearchEnabled) {
            AgentTool webSearch = new AgentTool();
            webSearch.toolId = "builtin-web-search";
            webSearch.name = "Web Search";
            webSearch.description = "Search the internet for up-to-date information";
            webSearch.type = ToolType.WEB_SEARCH;
            webSearch.enabled = true;
            webSearch.configuration = Map.of(
                "provider", "duckduckgo",
                "maxResults", "5"
            );
            tools.add(webSearch);
        }

        return Uni.createFrom().item(Response.ok(Map.of("tools", tools)).build());
    }
}
