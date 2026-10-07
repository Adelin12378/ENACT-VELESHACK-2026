package eu.enact.greencharge.model;

/**
 * Optional routing hints. The body may be empty ({}) — the service simply
 * returns the greenest available charger.
 */
public record RouteRequest(String connectorType) {
}
