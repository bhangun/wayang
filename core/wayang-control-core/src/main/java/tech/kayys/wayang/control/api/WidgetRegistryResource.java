package tech.kayys.wayang.control.api;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import tech.kayys.wayang.plugin.ControlPlaneWidgetRegistry;
import tech.kayys.wayang.plugin.UIWidgetDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * UI widget registry and management API.
 */
@Path("/api/v1/control-plane/widgets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Control Plane - Widgets", description = "UI widget registry")
public class WidgetRegistryResource {

    private static final Logger LOG = Logger.getLogger(WidgetRegistryResource.class);

    @Inject
    ControlPlaneWidgetRegistry widgetRegistry;

    /**
     * Get all widgets.
     */
    @GET
    @Operation(summary = "List all registered widgets")
    public Uni<List<WidgetDTO>> listWidgets(
            @QueryParam("type") String type) {

        return Uni.createFrom().item(() -> {
            List<UIWidgetDefinition> widgets = widgetRegistry.getAll();

            return widgets.stream()
                    .filter(w -> type == null || type.equals(w.type))
                    .map(w -> new WidgetDTO(
                            w.widgetId,
                            w.type,
                            new ArrayList<>(w.supportedDataTypes),
                            new ArrayList<>(w.capabilities),
                            w.entryPoint,
                            w.version,
                            w.defaultProps))
                    .toList();
        });
    }

    /**
     * Get widget details.
     */
    @GET
    @Path("/{widgetId}")
    @Operation(summary = "Get widget details")
    public Uni<RestResponse<WidgetDTO>> getWidget(
            @PathParam("widgetId") String widgetId) {

        return Uni.createFrom().item(() -> {
            UIWidgetDefinition widget = widgetRegistry.get(widgetId);
            if (widget == null) {
                return RestResponse.notFound();
            }

            return RestResponse.ok(new WidgetDTO(
                    widget.widgetId,
                    widget.type,
                    new ArrayList<>(widget.supportedDataTypes),
                    new ArrayList<>(widget.capabilities),
                    widget.entryPoint,
                    widget.version,
                    widget.defaultProps));
        });
    }

    /**
     * Register widget.
     */
    @POST
    @Operation(summary = "Register widget")
    public Uni<RestResponse<WidgetRegistrationResponse>> registerWidget(
            @Valid WidgetRegistrationRequest request) {

        LOG.infof("Registering widget: %s (%s)", request.widgetId(), request.type());

        UIWidgetDefinition widget = new UIWidgetDefinition();
        widget.widgetId = request.widgetId();
        widget.type = request.type();
        widget.supportedDataTypes = new HashSet<>(request.supportedDataTypes());
        widget.capabilities = new HashSet<>(request.capabilities());
        widget.entryPoint = request.entryPoint();
        widget.version = request.version();
        widget.defaultProps = request.defaultProps();

        widgetRegistry.register(widget);

        return Uni.createFrom().item(RestResponse.ok(
                new WidgetRegistrationResponse(true, widget.widgetId, "Widget registered")));
    }
}

/**
 * Widget DTO for API responses
 */
record WidgetDTO(
        String widgetId,
        String type,
        List<String> supportedDataTypes,
        List<String> capabilities,
        String entryPoint,
        String version,
        Map<String, Object> defaultProps) {
}

/**
 * Widget registration request
 */
record WidgetRegistrationRequest(
        String widgetId,
        String type,
        List<String> supportedDataTypes,
        List<String> capabilities,
        String entryPoint,
        String version,
        Map<String, Object> defaultProps) {
}

/**
 * Widget registration response
 */
record WidgetRegistrationResponse(
        boolean success,
        String widgetId,
        String message) {
}
