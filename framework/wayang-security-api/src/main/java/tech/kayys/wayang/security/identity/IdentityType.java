package tech.kayys.wayang.security.identity;

/**
 * Classifies the type of principal in the Wayang security model.
 * Deliberately free of Keycloak, JWT, or protocol specifics.
 */
public enum IdentityType {
    USER,
    SERVICE,
    AGENT,
    SYSTEM,
    ANONYMOUS
}
