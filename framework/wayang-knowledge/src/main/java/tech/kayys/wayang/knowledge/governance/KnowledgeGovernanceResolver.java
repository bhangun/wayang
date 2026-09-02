package tech.kayys.wayang.knowledge.governance;

import tech.kayys.wayang.knowledge.KnowledgeContext;
import tech.kayys.wayang.knowledge.KnowledgeItem;

/**
 * Evaluates whether knowledge items are permitted to be retrieved or stored under the current governance scope.
 */
public interface KnowledgeGovernanceResolver {

    boolean isPermitted(KnowledgeItem item, KnowledgeContext context);
}
