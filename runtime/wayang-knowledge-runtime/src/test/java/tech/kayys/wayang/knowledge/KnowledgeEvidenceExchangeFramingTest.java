package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.framing.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceExchangeFramingTest {

    @Test
    public void testBinaryFrameCodecAndIntegrity() {
        BinaryKnowledgeEvidenceExchangeFrameCodec codec =
                new BinaryKnowledgeEvidenceExchangeFrameCodec();

        byte[] payload = "test payload data for evidence transfer".getBytes(StandardCharsets.UTF_8);

        KnowledgeEvidenceExchangeFrame frame = new KnowledgeEvidenceExchangeFrame(
                (byte) 1,
                KnowledgeEvidenceExchangeFrameType.DATA,
                EnumSet.noneOf(KnowledgeEvidenceExchangeFrameFlags.class),
                "sess-123",
                "stream-123",
                "req-123",
                42L,
                payload.length,
                payload,
                "sha256:abcd",
                Map.of()
        );

        byte[] encoded = codec.encode(frame);
        assertNotNull(encoded);

        KnowledgeEvidenceExchangeFrame decoded = codec.decode(encoded);
        assertNotNull(decoded);
        assertEquals(frame.type(), decoded.type());
        assertEquals(frame.streamId(), decoded.streamId());
        assertEquals(frame.sequence(), decoded.sequence());
        assertArrayEquals(frame.payload(), decoded.payload());
    }

    @Test
    public void testChunkAssemblyAndResumeToken() {
        DefaultKnowledgeEvidenceExchangeChunkAssembler assembler =
                new DefaultKnowledgeEvidenceExchangeChunkAssembler("s-1");

        byte[] chunk1Data = "Hello, ".getBytes(StandardCharsets.UTF_8);
        byte[] chunk2Data = "Wayang World!".getBytes(StandardCharsets.UTF_8);

        KnowledgeEvidenceExchangeChunk chunk1 = new KnowledgeEvidenceExchangeChunk(
                "art-1", "res-1", "s-1", 0L, 0L, chunk1Data.length + chunk2Data.length, chunk1Data, "fp1", true, false, Map.of()
        );

        KnowledgeEvidenceExchangeChunk chunk2 = new KnowledgeEvidenceExchangeChunk(
                "art-1", "res-1", "s-1", 1L, (long) chunk1Data.length, chunk1Data.length + chunk2Data.length, chunk2Data, "fp2", false, true, Map.of()
        );

        assembler.accept(chunk1);
        assembler.accept(chunk2);
        byte[] assembled = assembler.complete();
        assertNotNull(assembled);
        assertEquals("Hello, Wayang World!", new String(assembled, StandardCharsets.UTF_8));

        InMemoryKnowledgeEvidenceExchangeResumeService resumeService =
                new InMemoryKnowledgeEvidenceExchangeResumeService();

        Instant now = Instant.now();
        var token = resumeService.create("sess-1", "str-1", "art-1", "res-1", 1024, 5, "fp-hash", now);
        assertNotNull(token);
        assertEquals("sess-1", token.sessionId());
        assertEquals(1024, token.offset());

        var resolved = resumeService.resolve(token.tokenId(), now);
        assertNotNull(resolved);
        assertEquals(token.tokenId(), resolved.tokenId());
    }
}
