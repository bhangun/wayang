package tech.kayys.wayang.models.safety;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Safety gate for content filtering.
 * Performs pre and post-inference safety checks.
 */
@ApplicationScoped
@Slf4j
public class SafetyGate {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
    
    private static final Pattern SSN_PATTERN = 
        Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    
    private static final List<String> UNSAFE_KEYWORDS = List.of(
        "violence", "explicit", "illegal", "harmful"
    );
    
    /**
     * Check input request for safety violations.
     * 
     * @param request Model request
     * @return Safety check result
     */
    public Uni<SafetyCheck> checkInput(ModelRequest request) {
        log.debug("Performing pre-check for request: {}", request.getRequestId());
        
        List<SafetyCheck.Violation> violations = new ArrayList<>();
        
        // Check prompt content
        if (request.getPrompt() != null) {
            violations.addAll(checkContent(request.getPrompt()));
        }
        
        // Check messages
        if (request.getMessages() != null) {
            for (var message : request.getMessages()) {
                if (message.getContent() != null) {
                    violations.addAll(checkContent(message.getContent()));
                }
            }
        }
        
        if (violations.isEmpty()) {
            return Uni.createFrom().item(SafetyCheck.safe());
        }
        
        log.warn("Input safety violations detected: {}", violations.size());
        return Uni.createFrom().item(SafetyCheck.unsafe(violations));
    }
    
    /**
     * Check output response for safety violations.
     * 
     * @param response Model response
     * @return Safety check result with sanitized content
     */
    public Uni<SafetyCheck> checkOutput(ModelResponse response) {
        log.debug("Performing post-check for response: {}", response.getRequestId());
        
        if (response.getContent() == null) {
            return Uni.createFrom().item(SafetyCheck.safe());
        }
        
        List<SafetyCheck.Violation> violations = checkContent(response.getContent());
        
        if (violations.isEmpty()) {
            return Uni.createFrom().item(SafetyCheck.safe());
        }
        
        // Sanitize content
        String sanitized = sanitizeContent(response.getContent());
        
        log.warn("Output safety violations detected: {}, content sanitized", violations.size());
        
        return Uni.createFrom().item(SafetyCheck.builder()
            .safe(false)
            .confidenceScore(0.5)
            .violations(violations)
            .sanitizedContent(sanitized)
            .build());
    }
    
    private List<SafetyCheck.Violation> checkContent(String content) {
        List<SafetyCheck.Violation> violations = new ArrayList<>();
        
        // Check for PII
        violations.addAll(checkPII(content));
        
        // Check for unsafe keywords
        violations.addAll(checkUnsafeContent(content));
        
        return violations;
    }
    
    private List<SafetyCheck.Violation> checkPII(String content) {
        List<SafetyCheck.Violation> violations = new ArrayList<>();
        
        // Email detection
        Matcher emailMatcher = EMAIL_PATTERN.matcher(content);
        while (emailMatcher.find()) {
            violations.add(SafetyCheck.Violation.builder()
                .type("PII_EMAIL")
                .severity("MEDIUM")
                .description("Email address detected")
                .startIndex(emailMatcher.start())
                .endIndex(emailMatcher.end())
                .build());
        }
        
        // Phone number detection
        Matcher phoneMatcher = PHONE_PATTERN.matcher(content);
        while (phoneMatcher.find()) {
            violations.add(SafetyCheck.Violation.builder()
                .type("PII_PHONE")
                .severity("MEDIUM")
                .description("Phone number detected")
                .startIndex(phoneMatcher.start())
                .endIndex(phoneMatcher.end())
                .build());
        }
        
        // SSN detection
        Matcher ssnMatcher = SSN_PATTERN.matcher(content);
        while (ssnMatcher.find()) {
            violations.add(SafetyCheck.Violation.builder()
                .type("PII_SSN")
                .severity("HIGH")
                .description("Social Security Number detected")
                .startIndex(ssnMatcher.start())
                .endIndex(ssnMatcher.end())
                .build());
        }
        
        return violations;
    }
    
    private List<SafetyCheck.Violation> checkUnsafeContent(String content) {
        List<SafetyCheck.Violation> violations = new ArrayList<>();
        String lowerContent = content.toLowerCase();
        
        for (String keyword : UNSAFE_KEYWORDS) {
            if (lowerContent.contains(keyword)) {
                violations.add(SafetyCheck.Violation.builder()
                    .type("UNSAFE_CONTENT")
                    .severity("HIGH")
                    .description("Potentially unsafe keyword: " + keyword)
                    .startIndex(-1)
                    .endIndex(-1)
                    .build());
            }
        }
        
        return violations;
    }
    
    private String sanitizeContent(String content) {
        String sanitized = content;
        
        // Redact emails
        sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll("[EMAIL_REDACTED]");
        
        // Redact phones
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[PHONE_REDACTED]");
        
        // Redact SSN
        sanitized = SSN_PATTERN.matcher(sanitized).replaceAll("[SSN_REDACTED]");
        
        return sanitized;
    }
}