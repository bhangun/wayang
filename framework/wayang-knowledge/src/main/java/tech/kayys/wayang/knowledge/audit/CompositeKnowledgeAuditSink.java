package tech.kayys.wayang.knowledge.audit;

import java.util.List;
import java.util.Objects;

public final class CompositeKnowledgeAuditSink implements KnowledgeAuditSink {

    private final List<KnowledgeAuditSink> sinks;

    public CompositeKnowledgeAuditSink(List<KnowledgeAuditSink> sinks) {
        this.sinks = sinks == null ? List.of() : sinks.stream().filter(Objects::nonNull).toList();
    }

    @Override
    public void publish(KnowledgeAuditEvent event) {
        for (KnowledgeAuditSink sink : sinks) {
            sink.publish(event);
        }
    }
}
