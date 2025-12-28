package tech.kayys.wayang.plugin.dto;

public record PluginCapability(
        String name,
        String version,
        Map<String, Object> parameters) {
    public boolean satisfies(PluginRequirement requirement) {
        return name.equals(requirement.name()) &&
                version.compareTo(requirement.minVersion()) >= 0;
    }
}