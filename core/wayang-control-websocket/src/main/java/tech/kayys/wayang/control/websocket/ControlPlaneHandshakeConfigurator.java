package tech.kayys.wayang.control.websocket;

import java.util.List;
import java.util.Map;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class ControlPlaneHandshakeConfigurator extends ServerEndpointConfig.Configurator {

    public static final String AUTHORIZATION = "Authorization";
    public static final String TENANT_ID = "X-Tenant-Id";
    public static final String AUTHORIZATION_PROP = "ws.authorization";
    public static final String TENANT_ID_PROP = "ws.tenantId";

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        Map<String, List<String>> headers = request.getHeaders();
        sec.getUserProperties().put(AUTHORIZATION_PROP, firstHeader(headers, AUTHORIZATION));
        sec.getUserProperties().put(TENANT_ID_PROP, firstHeader(headers, TENANT_ID));
        super.modifyHandshake(sec, request, response);
    }

    private String firstHeader(Map<String, List<String>> headers, String headerName) {
        if (headers == null || headerName == null) {
            return null;
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (!headerName.equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                return null;
            }
            return values.get(0);
        }
        return null;
    }
}
