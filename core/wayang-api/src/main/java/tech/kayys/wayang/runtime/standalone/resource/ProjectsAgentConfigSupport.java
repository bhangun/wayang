package tech.kayys.wayang.runtime.standalone.resource;

import tech.kayys.wayang.security.secrets.core.SecretManager;
import tech.kayys.wayang.security.secrets.dto.RetrieveSecretRequest;
import tech.kayys.wayang.security.secrets.dto.Secret;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ProjectsAgentConfigSupport {
    private ProjectsAgentConfigSupport() {
    }

    static Map<String, Object> summarizeAgentConfigCoverage(
            Map<String, Object> specPayload,
            String tenantId,
            SecretManager secretManager) {
        if (specPayload == null || specPayload.isEmpty()) {
            return Map.of();
        }

        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agentConfigNodes", 0);
        summary.put("providerModeAuto", 0);
        summary.put("providerModeLocal", 0);
        summary.put("providerModeCloud", 0);
        summary.put("localProviderConfigs", 0);
        summary.put("cloudProviderConfigs", 0);
        summary.put("credentialRefs", 0);
        summary.put("credentialRefsResolved", 0);
        summary.put("credentialRefsMissing", 0);
        summary.put("vaultConfigs", 0);

        final List<String> missingSecretPaths = new ArrayList<>();
        final Set<String> backends = new HashSet<>();
        final Map<String, String> rootVaultContext = extractRootVaultContext(specPayload, tenantId);
        scanConfigObject(specPayload, tenantId, rootVaultContext, summary, missingSecretPaths, backends, secretManager);

        int agentConfigNodes = intValue(summary.get("agentConfigNodes"));
        int credentialRefs = intValue(summary.get("credentialRefs"));
        int vaultConfigs = intValue(summary.get("vaultConfigs"));
        int localProviderConfigs = intValue(summary.get("localProviderConfigs"));
        int cloudProviderConfigs = intValue(summary.get("cloudProviderConfigs"));
        if (agentConfigNodes == 0
                && credentialRefs == 0
                && vaultConfigs == 0
                && localProviderConfigs == 0
                && cloudProviderConfigs == 0) {
            return Map.of();
        }

        summary.put("secretBackends", backends.stream().sorted().toList());
        if (!missingSecretPaths.isEmpty()) {
            summary.put("missingSecretPaths", missingSecretPaths);
        }
        summary.put("secretResolutionChecked", secretManager != null);
        return summary;
    }

    static Map<String, Object> resolveCredentialInputs(
            Map<String, Object> specPayload,
            String tenantId,
            SecretManager secretManager) {
        if (secretManager == null || specPayload == null || specPayload.isEmpty()) {
            return Map.of();
        }

        final List<Map<String, String>> refs = new ArrayList<>();
        final Map<String, String> rootVaultContext = extractRootVaultContext(specPayload, tenantId);
        collectCredentialRefs(specPayload, tenantId, rootVaultContext, refs);
        if (refs.isEmpty()) {
            return Map.of();
        }

        final Map<String, Object> resolved = new LinkedHashMap<>();
        for (Map<String, String> ref : refs) {
            String name = ref.getOrDefault("name", "secret");
            String path = ref.get("path");
            if (path == null || path.isBlank()) {
                continue;
            }
            String refTenant = ref.getOrDefault("tenantId", tenantId);
            String key = ref.get("key");

            try {
                Secret secret = secretManager.retrieve(RetrieveSecretRequest.latest(refTenant, path)).await().indefinitely();
                if (secret == null || secret.data() == null || secret.data().isEmpty()) {
                    continue;
                }

                String value = null;
                if (key != null && !key.isBlank()) {
                    value = secret.data().get(key);
                }
                if ((value == null || value.isBlank()) && secret.data().size() == 1) {
                    value = secret.data().values().iterator().next();
                }
                if (value != null && !value.isBlank()) {
                    resolved.put(name, value);
                }
            } catch (Exception ignored) {
                // Keep execution flow non-fatal; unresolved secrets are reflected in coverage summary.
            }
        }

        return resolved;
    }

    @SuppressWarnings("unchecked")
    private static void collectCredentialRefs(
            Object value,
            String defaultTenant,
            Map<String, String> inheritedVault,
            List<Map<String, String>> refs) {
        if (value == null) {
            return;
        }

        if (value instanceof List<?> list) {
            list.forEach(item -> collectCredentialRefs(item, defaultTenant, inheritedVault, refs));
            return;
        }

        if (!(value instanceof Map<?, ?> raw)) {
            return;
        }

        final Map<String, Object> map = new LinkedHashMap<>();
        raw.forEach((k, v) -> map.put(String.valueOf(k), v));

        Map<String, String> vaultContext = new HashMap<>(inheritedVault);
        applyVaultContext(vaultContext, mapValue(map.get("vault")));

        Object credentialRefs = map.get("credentialRefs");
        if (credentialRefs instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> refMap = mapValue(item);
                if (refMap.isEmpty()) {
                    continue;
                }
                final Map<String, String> ref = new LinkedHashMap<>();
                String name = optionalStringValue(refMap.get("name"));
                String path = optionalStringValue(refMap.get("path"));
                String key = optionalStringValue(refMap.get("key"));
                String tenant = optionalStringValue(refMap.get("tenantId"));

                if (name != null) {
                    ref.put("name", name);
                }
                if (key != null) {
                    ref.put("key", key);
                }
                if (tenant != null) {
                    ref.put("tenantId", tenant);
                } else if (vaultContext.containsKey("tenantId")) {
                    ref.put("tenantId", vaultContext.get("tenantId"));
                } else {
                    ref.put("tenantId", defaultTenant);
                }

                if (path != null) {
                    if (!path.startsWith("/") && vaultContext.containsKey("pathPrefix")) {
                        String prefix = vaultContext.get("pathPrefix");
                        if (prefix != null && !prefix.isBlank()) {
                            String normalizedPrefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
                            if (path.equals(normalizedPrefix) || path.startsWith(normalizedPrefix + "/")) {
                                ref.put("path", path);
                            } else {
                                ref.put("path", normalizedPrefix + "/" + path);
                            }
                        } else {
                            ref.put("path", path);
                        }
                    } else {
                        ref.put("path", path);
                    }
                }

                refs.add(ref);
            }
        }

        map.values().forEach(v -> collectCredentialRefs(v, defaultTenant, vaultContext, refs));
    }

    @SuppressWarnings("unchecked")
    private static void scanConfigObject(
            Object value,
            String tenantId,
            Map<String, String> inheritedVault,
            Map<String, Object> summary,
            List<String> missingSecretPaths,
            Set<String> backends,
            SecretManager secretManager) {
        if (value == null) {
            return;
        }
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = new LinkedHashMap<>();
            rawMap.forEach((k, v) -> map.put(String.valueOf(k), v));
            Map<String, String> vaultContext = new HashMap<>(inheritedVault);

            boolean hasAgentConfigSignal = map.containsKey("providerMode")
                    || map.containsKey("localProvider")
                    || map.containsKey("cloudProvider")
                    || map.containsKey("credentialRefs")
                    || map.containsKey("vault");
            if (hasAgentConfigSignal) {
                increment(summary, "agentConfigNodes");
            }

            String providerMode = optionalStringValue(map.get("providerMode"));
            if (providerMode != null) {
                switch (providerMode.toLowerCase()) {
                    case "auto" -> increment(summary, "providerModeAuto");
                    case "local" -> increment(summary, "providerModeLocal");
                    case "cloud" -> increment(summary, "providerModeCloud");
                    default -> {
                    }
                }
            }

            if (map.get("localProvider") instanceof Map<?, ?>) {
                increment(summary, "localProviderConfigs");
            }
            if (map.get("cloudProvider") instanceof Map<?, ?>) {
                increment(summary, "cloudProviderConfigs");
            }

            Map<String, Object> vaultConfig = mapValue(map.get("vault"));
            if (!vaultConfig.isEmpty()) {
                increment(summary, "vaultConfigs");
            }
            applyVaultContext(vaultContext, vaultConfig);
            String backend = optionalStringValue(vaultConfig.get("backend"));
            if (backend == null) {
                backend = vaultContext.get("backend");
            }
            if (backend != null) {
                backends.add(backend);
            }

            Object refs = map.get("credentialRefs");
            if (refs instanceof List<?> refList) {
                for (Object ref : refList) {
                    Map<String, Object> refMap = mapValue(ref);
                    if (refMap.isEmpty()) {
                        continue;
                    }
                    increment(summary, "credentialRefs");

                    backend = optionalStringValue(refMap.get("backend"));
                    if (backend == null) {
                        backend = vaultContext.get("backend");
                    }
                    if (backend != null) {
                        backends.add(backend);
                    }

                    String path = optionalStringValue(refMap.get("path"));
                    if (path != null && !path.startsWith("/") && vaultContext.containsKey("pathPrefix")) {
                        String prefix = vaultContext.get("pathPrefix");
                        if (prefix != null && !prefix.isBlank()) {
                            String normalizedPrefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
                            if (!path.equals(normalizedPrefix) && !path.startsWith(normalizedPrefix + "/")) {
                                path = normalizedPrefix + "/" + path;
                            }
                        }
                    }
                    if (path == null) {
                        increment(summary, "credentialRefsMissing");
                        continue;
                    }

                    if (secretManager != null) {
                        try {
                            String refTenant = optionalStringValue(refMap.get("tenantId"));
                            if (refTenant == null) {
                                refTenant = vaultContext.get("tenantId");
                            }
                            if (refTenant == null) {
                                refTenant = tenantId;
                            }
                            boolean exists = secretManager.exists(refTenant, path).await().indefinitely();
                            if (exists) {
                                increment(summary, "credentialRefsResolved");
                            } else {
                                increment(summary, "credentialRefsMissing");
                                if (missingSecretPaths.size() < 10) {
                                    missingSecretPaths.add(path);
                                }
                            }
                        } catch (Exception ignored) {
                            increment(summary, "credentialRefsMissing");
                            if (missingSecretPaths.size() < 10) {
                                missingSecretPaths.add(path);
                            }
                        }
                    }
                }
            }

            map.values().forEach(v -> scanConfigObject(
                    v,
                    tenantId,
                    vaultContext,
                    summary,
                    missingSecretPaths,
                    backends,
                    secretManager));
            return;
        }
        if (value instanceof List<?> list) {
            list.forEach(v -> scanConfigObject(
                    v,
                    tenantId,
                    inheritedVault,
                    summary,
                    missingSecretPaths,
                    backends,
                    secretManager));
        }
    }

    private static Map<String, String> extractRootVaultContext(Map<String, Object> specPayload, String tenantId) {
        Map<String, String> context = new HashMap<>();
        if (tenantId != null && !tenantId.isBlank()) {
            context.put("tenantId", tenantId);
        }
        Map<String, Object> extensions = mapValue(specPayload.get("extensions"));
        applyVaultContext(context, mapValue(extensions.get("vault")));
        return context;
    }

    private static void applyVaultContext(Map<String, String> context, Map<String, Object> vaultConfig) {
        if (context == null || vaultConfig == null || vaultConfig.isEmpty()) {
            return;
        }
        String tenant = optionalStringValue(vaultConfig.get("tenantId"));
        String prefix = optionalStringValue(vaultConfig.get("pathPrefix"));
        String backend = optionalStringValue(vaultConfig.get("backend"));
        if (tenant != null) {
            context.put("tenantId", tenant);
        }
        if (prefix != null) {
            context.put("pathPrefix", prefix);
        }
        if (backend != null) {
            context.put("backend", backend);
        }
    }

    private static void increment(Map<String, Object> summary, String key) {
        summary.put(key, intValue(summary.get(key)) + 1);
    }

    private static int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            final Map<String, Object> result = new HashMap<>();
            map.forEach((key, value) -> result.put(String.valueOf(key), value));
            return result;
        }
        return new HashMap<>();
    }

    private static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = String.valueOf(raw).trim();
        return value.isEmpty() ? null : value;
    }
}
