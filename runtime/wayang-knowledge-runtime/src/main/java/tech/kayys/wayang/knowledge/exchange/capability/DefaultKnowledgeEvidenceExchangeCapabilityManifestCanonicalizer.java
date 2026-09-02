package tech.kayys.wayang.knowledge.exchange.capability;

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


import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class DefaultKnowledgeEvidenceExchangeCapabilityManifestCanonicalizer
        implements KnowledgeEvidenceExchangeCapabilityManifestCanonicalizer {

    @Override
    public byte[] canonicalize(
            KnowledgeEvidenceExchangeCapabilityManifest manifest
    ) {

        String capabilities =
                manifest.capabilities()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        c -> c.type().name()
                                )
                        )
                        .map(capability ->
                                capability.type().name()
                                        + ":"
                                        + capability.supported()
                                        + ":"
                                        + capability.algorithms()
                                                .stream()
                                                .sorted()
                                                .collect(
                                                        Collectors.joining(",")
                                                )
                                        + ":"
                                        + capability.formats()
                                                .stream()
                                                .sorted()
                                                .collect(
                                                        Collectors.joining(",")
                                                )
                                        + ":"
                                        + capability.maxArtifactBytes()
                                        + ":"
                                        + capability.required()
                        )
                        .collect(Collectors.joining("\n"));

        String canonical = String.join(
                "\n",
                manifest.runtimeId(),
                nullSafe(manifest.identityVersion()),
                nullSafe(manifest.identityFingerprint()),
                manifest.protocolVersion().toString(),
                capabilities,
                manifest.issuedAt() == null
                        ? ""
                        : manifest.issuedAt().toString(),
                manifest.expiresAt() == null
                        ? ""
                        : manifest.expiresAt().toString()
        );

        return canonical.getBytes(
                StandardCharsets.UTF_8
        );
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
