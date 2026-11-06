package edu.bits.ooad.microgrid.application;

/**
 * Represents the external environmental and operational inputs for one simulation step in a scenario.
 */
public record ScenarioStep(
        int hour,
        double solarGenerationKw,
        double totalDemandKw,
        double criticalDemandKw,
        boolean gridAvailable,
        String description) {
}

