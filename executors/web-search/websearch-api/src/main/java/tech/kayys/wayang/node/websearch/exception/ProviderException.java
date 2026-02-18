package tech.kayys.wayang.node.websearch.exception;

public class ProviderException extends SearchException {
    private final String provider;
    private final Integer statusCode;
    private final Long retryAfterMillis;

    public ProviderException(String provider, String message) {
        super("Provider " + provider + ": " + message);
        this.provider = provider;
        this.statusCode = null;
        this.retryAfterMillis = null;
    }

    public ProviderException(String provider, int statusCode, String message) {
        this(provider, statusCode, message, null);
    }

    public ProviderException(String provider, int statusCode, String message, Long retryAfterMillis) {
        super("Provider " + provider + " (HTTP " + statusCode + "): " + message);
        this.provider = provider;
        this.statusCode = statusCode;
        this.retryAfterMillis = retryAfterMillis;
    }

    public String provider() {
        return provider;
    }

    public Integer statusCode() {
        return statusCode;
    }

    public Long retryAfterMillis() {
        return retryAfterMillis;
    }

    public boolean isRateLimited() {
        return statusCode != null && statusCode == 429;
    }

    public boolean isRetryable() {
        if (statusCode == null) {
            return true;
        }
        return statusCode == 408 || statusCode == 425 || statusCode == 429 || statusCode >= 500;
    }
}
