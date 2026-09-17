package tech.kayys.wayang.spi.capability;

import java.util.Objects;

/**
 * Strong typing for capabilities.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>tool / git</li>
 *   <li>model / text-generation</li>
 *   <li>memory / vector</li>
 *   <li>knowledge / graph</li>
 * </ul>
 */
public record CapabilityType(
        String category,
        String name
) {

    public CapabilityType {
        Objects.requireNonNull(category, "category cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        if (category.isBlank()) {
            throw new IllegalArgumentException("category cannot be blank");
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
    }

    public static CapabilityType of(String category, String name) {
        return new CapabilityType(category, name);
    }

    public String qualifiedName() {
        return category + ":" + name;
    }
}
