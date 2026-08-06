package tech.kayys.wayang.extension;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



import java.util.Objects;

/**
 * Semantic version implementation following SemVer 2.0.0.
 */
public record Version(
    int major,
    int minor,
    int patch,
    String prerelease,
    String build
) implements Comparable<Version> {
    
    public static final Version VERSION_1_0_0 = new Version(1, 0, 0, null, null);
    public static final Version UNSPECIFIED = new Version(0, 0, 0, null, null);
    
    public Version {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version components must be non-negative");
        }
    }
    
    public Version(int major, int minor, int patch) {
        this(major, minor, patch, null, null);
    }
    
    public Version(int major, int minor, int patch, String prerelease) {
        this(major, minor, patch, prerelease, null);
    }
    
    public static Version parse(String versionString) {
        if (versionString == null || versionString.isEmpty()) {
            return UNSPECIFIED;
        }
        
        String[] parts = versionString.split("\\+");
        String versionPart = parts[0];
        String buildPart = parts.length > 1 ? parts[1] : null;
        
        String[] versionParts = versionPart.split("-");
        String corePart = versionParts[0];
        String prereleasePart = versionParts.length > 1 ? versionParts[1] : null;
        
        String[] numbers = corePart.split("\\.");
        if (numbers.length < 3) {
            throw new IllegalArgumentException("Version must have at least major.minor.patch");
        }
        
        return new Version(
            Integer.parseInt(numbers[0]),
            Integer.parseInt(numbers[1]),
            Integer.parseInt(numbers[2]),
            prereleasePart,
            buildPart
        );
    }
    
    public boolean isPrerelease() {
        return prerelease != null && !prerelease.isEmpty();
    }
    
    public boolean isSnapshot() {
        return isPrerelease() && prerelease.contains("SNAPSHOT");
    }
    
    @Override
    public int compareTo(Version other) {
        if (other == null) return 1;
        
        int majorCompare = Integer.compare(major, other.major);
        if (majorCompare != 0) return majorCompare;
        
        int minorCompare = Integer.compare(minor, other.minor);
        if (minorCompare != 0) return minorCompare;
        
        int patchCompare = Integer.compare(patch, other.patch);
        if (patchCompare != 0) return patchCompare;
        
        // Pre-release versions sort before release versions
        if (isPrerelease() && !other.isPrerelease()) return -1;
        if (!isPrerelease() && other.isPrerelease()) return 1;
        
        if (isPrerelease() && other.isPrerelease()) {
            return prerelease.compareTo(other.prerelease);
        }
        
        return 0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append('.').append(minor).append('.').append(patch);
        if (prerelease != null) {
            sb.append('-').append(prerelease);
        }
        if (build != null) {
            sb.append('+').append(build);
        }
        return sb.toString();
    }
}