package tech.kayys.wayang.core;

import tech.kayys.wayang.resource.Modality;
import java.util.Set;

/**
 * Encapsulates the requirements derived from the planned context,
 * allowing the ModelRouter to select a capable provider.
 *
 * @param modalities The modalities present in the context that the provider must support.
 * @param requiresToolCalling Whether the context expects tool calling capabilities.
 * @param requiredTokens An estimate of the context size needed.
 */
public record ContextRequirements(
    Set<Modality> modalities,
    boolean requiresToolCalling,
    int requiredTokens
) {}
