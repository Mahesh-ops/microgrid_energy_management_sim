package edu.bits.ooad.microgrid.web.dto;

import edu.bits.ooad.microgrid.domain.factory.BatteryType;
import edu.bits.ooad.microgrid.domain.strategy.StrategyType;

/**
 * Data Transfer Object for updating microgrid parameters from the web UI.
 */
public record SimulationConfigDto(
        Double solarGenerationKw,
        Double totalDemandKw,
        Double criticalDemandKw,
        BatteryType batteryType,
        Double batteryCapacityKwh,
        Double batterySocPercent,
        Double inverterRatingKw,
        Boolean gridAvailable,
        Double gridMaxImportKw,
        Double gridMaxExportKw,
        StrategyType strategyType,
        Double peakShavingTargetKw) {
}

