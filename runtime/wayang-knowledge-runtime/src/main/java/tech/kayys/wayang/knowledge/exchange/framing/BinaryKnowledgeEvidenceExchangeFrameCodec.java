package tech.kayys.wayang.knowledge.exchange.framing;

import tech.kayys.wayang.knowledge.*;
import tech.kayys.wayang.knowledge.seal.*;
import tech.kayys.wayang.knowledge.snapshot.*;
import tech.kayys.wayang.knowledge.snapshot.pack.*;
import tech.kayys.wayang.knowledge.snapshot.artifact.*;
import tech.kayys.wayang.knowledge.snapshot.merkle.*;
import tech.kayys.wayang.knowledge.exchange.*;
import tech.kayys.wayang.knowledge.exchange.auth.*;
import tech.kayys.wayang.knowledge.exchange.session.*;
import tech.kayys.wayang.knowledge.exchange.binding.*;
import tech.kayys.wayang.knowledge.exchange.envelope.*;
import tech.kayys.wayang.knowledge.exchange.trust.*;
import tech.kayys.wayang.knowledge.exchange.identity.*;
import tech.kayys.wayang.knowledge.exchange.capability.*;
import tech.kayys.wayang.knowledge.exchange.protocol.*;
import tech.kayys.wayang.knowledge.exchange.transport.*;
import tech.kayys.wayang.knowledge.exchange.framing.*;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class BinaryKnowledgeEvidenceExchangeFrameCodec
        implements KnowledgeEvidenceExchangeFrameCodec {

    private static final int MAGIC = 0x57415947; // "WAYG"

    private static final int MAX_FRAME_SIZE =
            64 * 1024 * 1024;

    @Override
    public byte[] encode(
            KnowledgeEvidenceExchangeFrame frame
    ) {

        var output =
                new ByteArrayOutputStream();

        encode(frame, output);

        return output.toByteArray();
    }

    @Override
    public void encode(
            KnowledgeEvidenceExchangeFrame frame,
            OutputStream output
    ) {

        try {

            var data =
                    new DataOutputStream(output);

            data.writeInt(MAGIC);

            data.writeByte(
                    frame.version()
            );

            data.writeByte(
                    frame.type().ordinal()
            );

            int flags = 0;

            for (var flag : frame.flags()) {
                flags |= 1 << flag.ordinal();
            }

            data.writeInt(flags);

            writeString(
                    data,
                    frame.sessionId()
            );

            writeString(
                    data,
                    frame.streamId()
            );

            writeString(
                    data,
                    frame.requestId()
            );

            data.writeLong(
                    frame.sequence()
            );

            data.writeLong(
                    frame.payloadLength()
            );

            data.writeInt(
                    frame.metadata().size()
            );

            for (var entry :
                    frame.metadata().entrySet()) {

                writeString(
                        data,
                        entry.getKey()
                );

                writeString(
                        data,
                        entry.getValue()
                );
            }

            writeString(
                    data,
                    frame.payloadFingerprint()
            );

            data.writeInt(
                    frame.payload().length
            );

            data.write(
                    frame.payload()
            );

            data.flush();

        } catch (IOException e) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Unable to encode frame",
                    e
            );
        }
    }

    @Override
    public KnowledgeEvidenceExchangeFrame decode(
            byte[] bytes
    ) {

        return decode(
                new ByteArrayInputStream(bytes)
        );
    }

    @Override
    public KnowledgeEvidenceExchangeFrame decode(
            InputStream input
    ) {

        try {

            var data =
                    new DataInputStream(input);

            int magic =
                    data.readInt();

            if (magic != MAGIC) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Invalid Wayang frame magic"
                );
            }

            byte version =
                    data.readByte();

            int typeOrdinal =
                    data.readUnsignedByte();

            var types =
                    KnowledgeEvidenceExchangeFrameType.values();

            if (typeOrdinal >= types.length) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Unknown frame type"
                );
            }

            var type =
                    types[typeOrdinal];

            int flagsBits =
                    data.readInt();

            var flags =
                    EnumSet.noneOf(
                            KnowledgeEvidenceExchangeFrameFlags.class
                    );

            for (var flag :
                    KnowledgeEvidenceExchangeFrameFlags.values()) {

                if ((flagsBits &
                        (1 << flag.ordinal())) != 0) {

                    flags.add(flag);
                }
            }

            String sessionId =
                    readString(data);

            String streamId =
                    readString(data);

            String requestId =
                    readString(data);

            long sequence =
                    data.readLong();

            long payloadLength =
                    data.readLong();

            if (payloadLength < 0 ||
                    payloadLength > MAX_FRAME_SIZE) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Invalid payload length: "
                                + payloadLength
                );
            }

            int metadataSize =
                    data.readInt();

            if (metadataSize < 0 ||
                    metadataSize > 1024) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Invalid metadata size"
                );
            }

            var metadata =
                    new java.util.HashMap<String, String>();

            for (int i = 0;
                 i < metadataSize;
                 i++) {

                metadata.put(
                        readString(data),
                        readString(data)
                );
            }

            String fingerprint =
                    readString(data);

            int actualLength =
                    data.readInt();

            if (actualLength != payloadLength ||
                    actualLength < 0 ||
                    actualLength > MAX_FRAME_SIZE) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Payload length mismatch"
                );
            }

            byte[] payload =
                    new byte[actualLength];

            data.readFully(payload);

            return new KnowledgeEvidenceExchangeFrame(
                    version,
                    type,
                    flags,
                    sessionId,
                    streamId,
                    requestId,
                    sequence,
                    payloadLength,
                    payload,
                    fingerprint,
                    metadata
            );

        } catch (EOFException e) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Truncated frame",
                    e
            );

        } catch (IOException e) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Unable to decode frame",
                    e
            );
        }
    }

    private static void writeString(
            DataOutputStream output,
            String value
    ) throws IOException {

        if (value == null) {
            output.writeInt(-1);
            return;
        }

        byte[] bytes =
                value.getBytes(StandardCharsets.UTF_8);

        output.writeInt(bytes.length);
        output.write(bytes);
    }

    private static String readString(
            DataInputStream input
    ) throws IOException {

        int length =
                input.readInt();

        if (length < 0) {
            return null;
        }

        if (length > MAX_FRAME_SIZE) {

            throw new IOException(
                    "String too large"
            );
        }

        byte[] bytes =
                new byte[length];

        input.readFully(bytes);

        return new String(
                bytes,
                StandardCharsets.UTF_8
        );
    }
}
