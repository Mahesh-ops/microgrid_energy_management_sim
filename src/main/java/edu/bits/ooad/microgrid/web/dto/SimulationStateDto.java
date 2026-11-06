package edu.bits.ooad.microgrid.web.dto;

import edu.bits.ooad.microgrid.domain.model.SimulationResult;

import java.util.List;

/**
 * Data Transfer Object carrying complete dashboard state, latest outcomes,
 * and historical metrics to the web frontend.
 */
public record SimulationStateDto(
        int stepIndex,
        String operatingState,
        boolean gridAvailable,
        String strategyName,
        String strategyType,
        double solarGenerationKw,
        double totalDemandKw,
        double criticalDemandKw,
        String batteryName,
        String batteryTechnology,
        double batterySocPercent,
        double batteryMinSocPercent,
        double batteryMaxSocPercent,
        double batteryCapacityKwh,
        double inverterRatingKw,
        Double peakShavingTargetKw,
        SimulationResult latestResult,
        List<SimulationResult> history,
        List<String> transitionLogs,
        double totalSolarGeneratedKwh,
        double totalSolarConsumedKwh,
        double totalSolarExportedKwh,
        double totalGridImportedKwh,
        double totalLoadServedKwh,
        double totalUnmetLoadKwh,
        double totalUnmetCriticalLoadKwh,
        double totalSimulatedHours) {
}

