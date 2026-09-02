package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.protocol.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceExchangeProtocolTest {

    @Test
    public void testProtocolStateMachineTransitions() {
        DefaultKnowledgeEvidenceExchangeProtocolStateMachine stateMachine =
                new DefaultKnowledgeEvidenceExchangeProtocolStateMachine();

        assertEquals(KnowledgeEvidenceExchangeProtocolState.NEW, stateMachine.state());
        assertTrue(stateMachine.canAccept(KnowledgeEvidenceExchangeProtocolMessageType.HELLO));

        stateMachine.transition(KnowledgeEvidenceExchangeProtocolMessageType.HELLO);
        assertEquals(KnowledgeEvidenceExchangeProtocolState.HELLO_SENT, stateMachine.state());

        assertTrue(stateMachine.canAccept(KnowledgeEvidenceExchangeProtocolMessageType.IDENTITY));
        stateMachine.transition(KnowledgeEvidenceExchangeProtocolMessageType.IDENTITY);
        assertEquals(KnowledgeEvidenceExchangeProtocolState.IDENTITY_SENT, stateMachine.state());
    }
}
