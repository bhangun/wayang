package tech.kayys.wayang.knowledge.exchange.membership;

public interface KnowledgeAnswerResolutionMembershipChangeValidator {
    boolean validate(
            KnowledgeAnswerResolutionMembershipChange change,
            KnowledgeAnswerResolutionMembershipSet current);
}
