package tech.kayys.wayang.control.dto;

/**
 * Catalog entry for integration pattern presets.
 */
public record PatternCatalogEntry(
        String id,
        String name,
        String description,
        String category,
        EIPPatternType patternType,
        String usageGuide) {
}
