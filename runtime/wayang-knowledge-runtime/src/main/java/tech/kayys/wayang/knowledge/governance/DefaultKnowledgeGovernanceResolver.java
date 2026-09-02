package tech.kayys.wayang.knowledge.governance;

import tech.kayys.wayang.knowledge.KnowledgeContext;
import tech.kayys.wayang.knowledge.KnowledgeItem;

public class DefaultKnowledgeGovernanceResolver implements KnowledgeGovernanceResolver {

    @Override
    public boolean isPermitted(KnowledgeItem item, KnowledgeContext context) {
        if (item == null) return false;
        if (context == null) return true;

        // If item is restricted, ensure scope matches or admin context
        if (item.classification() == KnowledgeClassification.RESTRICTED) {
            String itemScope = item.metadata().getOrDefault("scope", "default").toString();
            return context.scope().equalsIgnoreCase(itemScope);
        }

        return true;
    }
}
