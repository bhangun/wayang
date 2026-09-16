package tech.kayys.wayang.api.rest.security;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityDelegationResourceTest {

    private SecurityDelegationResource resource;
    private SecurityProducer producer;

    @BeforeEach
    void setUp() {
        producer = new SecurityProducer();
        resource = new SecurityDelegationResource();
        resource.delegationService = producer.produceDelegationService(
                producer.produceAttenuator(),
                producer.producePolicy()
        );
    }

    @Test
    void createsDelegationSuccessfully() {
        SecurityDelegationResource.CreateDelegationRequest request = new SecurityDelegationResource.CreateDelegationRequest(
                "agent-worker-1",
                List.of("workspace.read", "code.analyze"),
                List.of("execute"),
                2,
                1800
        );

        Response response = resource.createDelegation("tenant-alpha", "supervisor-agent", request);

        assertEquals(201, response.getStatus());
        SecurityDelegationResource.DelegationResponse entity = (SecurityDelegationResource.DelegationResponse) response.getEntity();

        assertNotNull(entity);
        assertNotNull(entity.delegationId());
        assertTrue(entity.delegationId().startsWith("del-"));
        assertEquals(List.of("agent-worker-1"), entity.targetAgentIds());
        assertEquals(List.of("workspace.read", "code.analyze"), entity.allowedCapabilities());
        assertEquals(List.of("execute"), entity.allowedActions());
        assertEquals(2, entity.maxHops());
        assertNotNull(entity.issuedAt());
        assertNotNull(entity.expiresAt());
    }

    @Test
    void rejectsEmptyAudience() {
        SecurityDelegationResource.CreateDelegationRequest request = new SecurityDelegationResource.CreateDelegationRequest(
                "",
                List.of("*"),
                List.of("*"),
                1,
                300
        );

        Response response = resource.createDelegation(null, null, request);
        assertEquals(400, response.getStatus());
    }

    @Test
    void statusEndpointReturnsSecurityInfo() {
        Response response = resource.status();
        assertEquals(200, response.getStatus());

        Map<?, ?> map = (Map<?, ?>) response.getEntity();
        assertEquals(true, map.get("delegationEnabled"));
    }
}
