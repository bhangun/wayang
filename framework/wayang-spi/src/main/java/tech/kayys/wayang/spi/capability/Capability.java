package tech.kayys.wayang.spi.capability;

/**
 * Universal capability interface in Wayang.
 *
 * <p>Represents an actionable functionality exposed by a plugin or the platform.</p>
 */
public interface Capability {

    /**
     * Unique identifier of this capability (e.g. "wayang.browser", "model.text").
     */
    String id();

    /**
     * Type classification of this capability.
     */
    CapabilityType type();

    /**
     * Metadata descriptor for this capability.
     */
    CapabilityDescriptor descriptor();
}
