package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.protocol.*;
import tech.kayys.wayang.knowledge.exchange.transport.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceExchangeTransportTest {

    @Test
    public void testInProcessTransportCommunication() throws Exception {
        var endpoint = new KnowledgeEvidenceExchangeProtocolEndpoint() {
            @Override
            public KnowledgeEvidenceExchangeProtocolMessage<?> handle(KnowledgeEvidenceExchangeProtocolMessage<?> message) {
                return message;
            }
        };

        InProcessKnowledgeEvidenceExchangeTransport transport =
                new InProcessKnowledgeEvidenceExchangeTransport(endpoint);

        var address = new KnowledgeEvidenceExchangeTransportAddress(
                KnowledgeEvidenceExchangeTransportType.IN_PROCESS, "in-process", "local", null, "/direct", Map.of()
        );

        var conn = transport.connect(address).toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertNotNull(conn);
        assertEquals(KnowledgeEvidenceExchangeTransportState.CONNECTED, conn.state());

        Instant now = Instant.now();
        var envelope = new KnowledgeEvidenceExchangeProtocolEnvelope(
                "msg-1", KnowledgeEvidenceExchangeProtocolMessageType.HELLO, "1.0",
                "rt-1", "rt-2", "sess-1", "corr-1", "nonce-1", now, now.plusSeconds(300), "fp-payload", Map.of()
        );

        var protoMsg = new KnowledgeEvidenceExchangeProtocolMessage<>(envelope, "payload-content");
        var transportMsg = new KnowledgeEvidenceExchangeTransportMessage("msg-1", protoMsg);
        var request = new KnowledgeEvidenceExchangeTransportRequest(
                "req-1", "sess-1", transportMsg, now, now.plusSeconds(60), Duration.ofSeconds(30), false, Map.of()
        );

        var response = conn.send(request).toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertTrue(response.success());
        assertNotNull(response.message());
    }
}
