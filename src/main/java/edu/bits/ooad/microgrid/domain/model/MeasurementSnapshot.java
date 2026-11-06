package edu.bits.ooad.microgrid.domain.model;

/**
 * Immutable snapshot of all component measurements and constraints collected
 * at the start of a single control step.
 */
public record MeasurementSnapshot(
        double solarGenerationKw,
        double totalDemandKw,
        double criticalDemandKw,
        double batterySocPercent,
        double batteryMinSocPercent,
        double batteryMaxSocPercent,
        double batteryFeasibleChargeKw,
        double batteryFeasibleDischargeKw,
        double inverterMaxChargeKw,
        double inverterMaxDischargeKw,
        boolean gridAvailable,
        double gridMaxImportKw,
        double gridMaxExportKw,
        double stepHours,
        Double peakShavingTargetKw) {

    public MeasurementSnapshot {
        if (solarGenerationKw < 0.0) {
            throw new IllegalArgumentException("Solar generation must be non-negative");
        }
        if (totalDemandKw < 0.0) {
            throw new IllegalArgumentException("Total demand must be non-negative");
        }
        if (criticalDemandKw < 0.0 || criticalDemandKw > totalDemandKw) {
            throw new IllegalArgumentException("Critical demand must be between 0 and total demand");
        }
        if (stepHours <= 0.0) {
            throw new IllegalArgumentException("Step duration must be positive");
        }
    }

    public double nonCriticalDemandKw() {
        return Math.max(0.0, totalDemandKw - criticalDemandKw);
    }
}

