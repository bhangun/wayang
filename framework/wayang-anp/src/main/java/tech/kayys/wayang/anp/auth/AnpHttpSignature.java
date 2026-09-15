package tech.kayys.wayang.anp.auth;

import java.util.Base64;
import java.util.Objects;

/**
 * Parsed representation of an ANP HTTP Signature.
 */
public record AnpHttpSignature(
        String keyId,
        String algorithm,
        String signedHeaders,
        byte[] signatureBytes
) {
    public AnpHttpSignature {
        Objects.requireNonNull(keyId, "keyId");
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(signedHeaders, "signedHeaders");
        Objects.requireNonNull(signatureBytes, "signatureBytes");
    }

    /** Formats as a standard Authorization or Signature HTTP header value. */
    public String toHeaderValue() {
        return String.format(
                "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",
                keyId, algorithm, signedHeaders, Base64.getEncoder().encodeToString(signatureBytes)
        );
    }

    /** Parses a Signature header value. */
    public static AnpHttpSignature parseHeader(String header) {
        Objects.requireNonNull(header, "header");
        String keyId = null;
        String algorithm = "ed25519";
        String headers = "(request-target) host date";
        byte[] sigBytes = new byte[0];

        for (String part : header.split(",")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2) {
                String k = kv[0].trim();
                String v = kv[1].trim().replace("\"", "");
                switch (k) {
                    case "keyId" -> keyId = v;
                    case "algorithm" -> algorithm = v;
                    case "headers" -> headers = v;
                    case "signature" -> sigBytes = Base64.getDecoder().decode(v);
                    default -> {}
                }
            }
        }
        if (keyId == null) {
            throw new IllegalArgumentException("Signature header missing keyId");
        }
        return new AnpHttpSignature(keyId, algorithm, headers, sigBytes);
    }
}
