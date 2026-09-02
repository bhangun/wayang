package tech.kayys.wayang.knowledge.exchange.session;

public final class DefaultKnowledgeEvidenceExchangeBindingCanonicalizer
        implements KnowledgeEvidenceExchangeBindingCanonicalizer {

    @Override
    public String canonicalize(KnowledgeEvidenceExchangeRequestBinding binding) {
        return String.join("|",
                safe(binding.requestId()),
                safe(binding.sessionId()),
                safe(binding.nonce()),
                safe(binding.runtimeId()),
                safe(binding.remoteRuntimeId()),
                safe(binding.tenantId()),
                safe(binding.workspaceId()),
                safe(binding.projectId()),
                binding.operation() != null ? binding.operation().name() : "",
                safe(binding.artifactId()),
                safe(binding.resourceId()),
                binding.issuedAt() != null ? binding.issuedAt().toString() : "",
                binding.expiresAt() != null ? binding.expiresAt().toString() : "",
                safe(binding.correlationId())
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
