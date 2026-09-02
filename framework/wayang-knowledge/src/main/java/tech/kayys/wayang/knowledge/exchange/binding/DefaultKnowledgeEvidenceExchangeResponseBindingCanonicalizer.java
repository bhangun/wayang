package tech.kayys.wayang.knowledge.exchange.binding;

public final class DefaultKnowledgeEvidenceExchangeResponseBindingCanonicalizer
        implements KnowledgeEvidenceExchangeResponseBindingCanonicalizer {

    @Override
    public String canonicalize(KnowledgeEvidenceExchangeResponseBinding binding) {
        return String.join("|",
                safe(binding.requestId()),
                safe(binding.sessionId()),
                safe(binding.requestNonce()),
                safe(binding.responseId()),
                safe(binding.runtimeId()),
                safe(binding.remoteRuntimeId()),
                safe(binding.tenantId()),
                safe(binding.workspaceId()),
                safe(binding.projectId()),
                binding.operation() != null ? binding.operation().name() : "",
                binding.artifactId() != null ? binding.artifactId().value() : "",
                safe(binding.resourceId()),
                Boolean.toString(binding.success()),
                safe(binding.contentFingerprint()),
                safe(binding.manifestFingerprint()),
                safe(binding.merkleProofFingerprint()),
                binding.issuedAt() != null ? binding.issuedAt().toString() : "",
                binding.expiresAt() != null ? binding.expiresAt().toString() : ""
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
