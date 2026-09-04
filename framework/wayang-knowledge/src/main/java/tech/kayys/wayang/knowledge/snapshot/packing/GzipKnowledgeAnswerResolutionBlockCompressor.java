package tech.kayys.wayang.knowledge.snapshot.packing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class GzipKnowledgeAnswerResolutionBlockCompressor
        implements KnowledgeAnswerResolutionBlockCompressor {

    @Override
    public String algorithm() {
        return "gzip";
    }

    @Override
    public byte[] compress(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
                gzip.write(input);
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to compress state block", e);
        }
    }

    @Override
    public byte[] decompress(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }

        try {
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(input));
            return gzip.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to decompress state block", e);
        }
    }
}
