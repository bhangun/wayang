package tech.kayys.wayang.guardrails.plugin;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.wayang.guardrails.detector.CheckPhase;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry for guardrails plugins that manages discovery and retrieval of guardrail detectors.
 */
@ApplicationScoped
public class GuardrailPluginRegistry {

    private static final Logger LOG = Logger.getLogger(GuardrailPluginRegistry.class);

    @Inject
    Instance<GuardrailDetectorPlugin> detectorPlugins;

    private volatile List<GuardrailDetectorPlugin> cachedPreExecutionDetectors;
    private volatile List<GuardrailDetectorPlugin> cachedPostExecutionDetectors;

    /**
     * Get all registered guardrail detector plugins.
     */
    public List<GuardrailDetectorPlugin> getAllDetectorPlugins() {
        List<GuardrailDetectorPlugin> plugins = new ArrayList<>();
        
        if (detectorPlugins.isResolvable()) {
            for (GuardrailDetectorPlugin plugin : detectorPlugins) {
                plugins.add(plugin);
            }
        }
        
        return plugins;
    }

    /**
     * Get all guardrail detector plugins applicable for pre-execution phase.
     */
    public List<GuardrailDetectorPlugin> getPreExecutionDetectors() {
        if (cachedPreExecutionDetectors == null) {
            synchronized (this) {
                if (cachedPreExecutionDetectors == null) {
                    cachedPreExecutionDetectors = getAllDetectorPlugins().stream()
                            .filter(plugin -> Arrays.asList(plugin.applicablePhases())
                                    .contains(GuardrailDetectorPlugin.CheckPhase.PRE_EXECUTION))
                            .collect(Collectors.toList());
                    
                    LOG.infof("Discovered %d pre-execution guardrail detectors", cachedPreExecutionDetectors.size());
                }
            }
        }
        
        return new ArrayList<>(cachedPreExecutionDetectors);
    }

    /**
     * Get all guardrail detector plugins applicable for post-execution phase.
     */
    public List<GuardrailDetectorPlugin> getPostExecutionDetectors() {
        if (cachedPostExecutionDetectors == null) {
            synchronized (this) {
                if (cachedPostExecutionDetectors == null) {
                    cachedPostExecutionDetectors = getAllDetectorPlugins().stream()
                            .filter(plugin -> Arrays.asList(plugin.applicablePhases())
                                    .contains(GuardrailDetectorPlugin.CheckPhase.POST_EXECUTION))
                            .collect(Collectors.toList());
                    
                    LOG.infof("Discovered %d post-execution guardrail detectors", cachedPostExecutionDetectors.size());
                }
            }
        }
        
        return new ArrayList<>(cachedPostExecutionDetectors);
    }

    /**
     * Get a specific guardrail detector by its ID.
     */
    public Optional<GuardrailDetectorPlugin> getDetectorById(String id) {
        return getAllDetectorPlugins().stream()
                .filter(plugin -> plugin.id().equals(id))
                .findFirst();
    }

    /**
     * Get all guardrail detectors by category.
     */
    public List<GuardrailDetectorPlugin> getDetectorsByCategory(String category) {
        return getAllDetectorPlugins().stream()
                .filter(plugin -> plugin.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Run all applicable detectors on the provided text for the given phase.
     */
    public Uni<List<tech.kayys.wayang.guardrails.detector.DetectionResult>> runDetectorsForPhase(
            String text, 
            CheckPhase phase) {
        
        List<GuardrailDetectorPlugin> detectors = switch (phase) {
            case PRE_EXECUTION -> getPreExecutionDetectors();
            case POST_EXECUTION -> getPostExecutionDetectors();
        };

        if (detectors.isEmpty()) {
            return Uni.createFrom().item(new ArrayList<>());
        }

        List<Uni<tech.kayys.wayang.guardrails.detector.DetectionResult>> detectorUnis = detectors.stream()
                .map(detector -> detector.detect(text))
                .collect(Collectors.toList());

        return Uni.combine().all().unis(detectorUnis).combinedWith(results -> (List<tech.kayys.wayang.guardrails.detector.DetectionResult>) results);
    }
}