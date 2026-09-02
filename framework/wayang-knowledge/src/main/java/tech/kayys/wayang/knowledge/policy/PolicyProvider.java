package tech.kayys.wayang.knowledge.policy;

import tech.kayys.wayang.knowledge.KnowledgeContext;

import java.util.List;

/**
 * Provider of applicable domain policies for a given context.
 */
public interface PolicyProvider {

    List<DecisionPolicy> applicablePolicies(
            PolicyContext policyContext,
            KnowledgeContext knowledgeContext
    );
}
