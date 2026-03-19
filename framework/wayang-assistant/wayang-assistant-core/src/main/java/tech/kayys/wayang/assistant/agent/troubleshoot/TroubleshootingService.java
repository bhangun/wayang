package tech.kayys.wayang.assistant.agent.troubleshoot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.DocSearchResult;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.ErrorTroubleshootingResult;
import tech.kayys.wayang.assistant.knowledge.KnowledgeSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for troubleshooting Wayang errors.
 */
@ApplicationScoped
public class TroubleshootingService {

    private static final Logger LOG = Logger.getLogger(TroubleshootingService.class);

    @Inject
    public KnowledgeSearchService searchService;

    public ErrorTroubleshootingResult troubleshootError(String errorMessage) {
        LOG.infof("Troubleshooting error: %s", errorMessage);

        List<DocSearchResult> docResults = new ArrayList<>(searchService.searchDocumentation(errorMessage));

        // Augment search with derived patterns
        extractErrorPatterns(errorMessage).forEach(p -> docResults.addAll(searchService.searchDocumentation(p)));

        String advice = generateTroubleshootingAdvice(errorMessage, docResults);
        return new ErrorTroubleshootingResult(errorMessage, advice, docResults);
    }

    private List<String> extractErrorPatterns(String error) {
        List<String> patterns = new ArrayList<>();
        if (error.contains(":")) {
            String beforeColon = error.split(":")[0].trim();
            if (beforeColon.contains(".")) {
                patterns.add(beforeColon.substring(beforeColon.lastIndexOf('.') + 1));
            }
            patterns.add(beforeColon.replace("Exception", "").replace("Error", "").trim());
        }
        var m = Pattern.compile("[A-Z]+-\\d+").matcher(error);
        while (m.find()) patterns.add(m.group());
        return patterns.stream().filter(p -> !p.isBlank()).collect(Collectors.toList());
    }

    private String generateTroubleshootingAdvice(String error, List<DocSearchResult> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Troubleshooting Guidance\n\n");

        List<DocSearchResult> validDocs = docs.stream()
                .filter(d -> !"No results found".equals(d.getTitle()))
                .limit(5)
                .collect(Collectors.toList());

        if (!validDocs.isEmpty()) {
            sb.append("### Relevant Documentation\n");
            for (int i = 0; i < validDocs.size(); i++) {
                DocSearchResult r = validDocs.get(i);
                sb.append(String.format("%d. **%s** — %s\n", i + 1, r.getTitle(), r.getSnippet()));
            }
            sb.append("\n");
        }

        sb.append("### General Steps\n");
        sb.append("1. Check `application.properties` for configuration syntax errors.\n");
        sb.append("2. Verify all required Maven dependencies are declared and up-to-date.\n");
        sb.append("3. Ensure supporting services (PostgreSQL, Kafka) are running.\n");
        sb.append("4. Review full logs for additional context around the error.\n");
        sb.append("5. Try `mvn -DskipTests clean install` on affected modules to rebuild.\n");
        sb.append("6. Consult the docs: https://wayang.github.io\n\n");

        String lower = error.toLowerCase();

        if (lower.contains("nullpointer") || lower.contains("npe")) {
            sb.append("### Null Pointer\n");
            sb.append("- Confirm all required `@ConfigProperty` values are set in `application.properties`.\n");
            sb.append("- Verify every `@Inject`-ed bean is annotated correctly (`@ApplicationScoped`, etc.).\n");
            sb.append("- Check that `Optional`-wrapped fields are actually populated before use.\n\n");
        }

        if (lower.contains("connection") || lower.contains("timeout") || lower.contains("refused")) {
            sb.append("### Connection / Timeout\n");
            sb.append("- Verify the service endpoint URL and port in `application.properties`.\n");
            sb.append("- Check network connectivity, firewall rules, and service health.\n");
            sb.append("- Increase `quarkus.datasource.jdbc.acquisition-timeout` if DB connections are exhausted.\n\n");
        }

        if (lower.contains("classnotfound") || lower.contains("noclassdef") || lower.contains("classloading")) {
            sb.append("### Class Loading\n");
            sb.append("- Run `mvn -DskipTests clean install` on the affected module and its dependencies.\n");
            sb.append("- Check for JAR version conflicts in your dependency tree (`mvn dependency:tree`).\n");
            sb.append("- Ensure Jandex indexes are up-to-date if using CDI (`mvn compile -Pjandex`).\n\n");
        }

        if (lower.contains("unsatisfied") || lower.contains("ambiguous") || lower.contains("cdi")) {
            sb.append("### CDI / Bean Discovery\n");
            sb.append("- Add the missing bean class to the correct module and verify it has a scope annotation.\n");
            sb.append("- Ensure all modules with beans include a `META-INF/beans.xml` or use `bean-discovery-mode=all`.\n");
            sb.append("- Run `mvn quarkus:build` to regenerate CDI metadata.\n\n");
        }

        if (lower.contains("guardrail") || lower.contains("policy") || lower.contains("blocked")) {
            sb.append("### Guardrail / Policy Violation\n");
            sb.append("- List active policies via `GET /api/v1/guardrails/policies`.\n");
            sb.append("- Check which policy was triggered in the error message / `triggeredPolicies` field.\n");
            sb.append("- Adjust the policy threshold or disable it temporarily via `DELETE /api/v1/guardrails/policies/{id}`.\n\n");
        }

        if (lower.contains("rag") || lower.contains("retrieval") || lower.contains("embedding")) {
            sb.append("### RAG / Retrieval\n");
            sb.append("- Confirm the collection name in your node configuration matches the ingested corpus.\n");
            sb.append("- Check `minSimilarity` – try lowering it (e.g. 0.7 → 0.5) if recall is too low.\n");
            sb.append("- Verify the embedding model and provider are correctly configured.\n\n");
        }

        return sb.toString();
    }
}
