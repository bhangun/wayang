package tech.kayys.wayang.prompt.store;

import java.util.logging.Logger;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import tech.kayys.wayang.prompt.core.PromptTemplate;

/**
 * ============================================================================
 * PromptTemplateJsonConverter — converts PromptTemplate ↔ String (JSON).
 * ============================================================================
 *
 * Uses Jackson for serialisation. In the standalone runtime this converter
 * is not needed because the in-memory store works directly with domain objects.
 *
 * Note: In a real project this would be a shared utility in a
 * {@code wayang-commons} module. It is placed here for self-containment.
 */
@Converter(autoApply = true)
public class PromptTemplateJsonConverter implements AttributeConverter<PromptTemplate, String> {

    private static final Logger LOG = Logger.getLogger(PromptTemplateJsonConverter.class.getName());

    // In production, inject com.fasterxml.jackson.databind.ObjectMapper via CDI.
    // Stubbed here for compilation independence.

    @Override
    public String convertToDatabaseColumn(PromptTemplate attribute) {
        if (attribute == null)
            return null;
        // Production: return objectMapper.writeValueAsString(attribute);
        return attribute.toString(); // placeholder
    }

    @Override
    public PromptTemplate convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        // Production: return objectMapper.readValue(dbData, PromptTemplate.class);
        return null; // placeholder — real implementation uses Jackson
    }
}
