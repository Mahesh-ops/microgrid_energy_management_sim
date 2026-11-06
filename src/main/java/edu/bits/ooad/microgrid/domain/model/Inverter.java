package edu.bits.ooad.microgrid.domain.model;

/**
 * Bidirectional power conversion system (PCS) interfacing battery storage to the AC bus.
 * Enforces AC conversion limits for both charge (rectification) and discharge (inversion).
 */
public class Inverter {

    private final double ratedPowerKw;
    private final double maxChargeLimitKw;
    private final double maxDischargeLimitKw;
    private final double efficiency;

    public Inverter(
            double ratedPowerKw,
            Double maxChargeLimitKw,
            Double maxDischargeLimitKw,
            double efficiency) {
        if (ratedPowerKw <= 0.0) {
            throw new IllegalArgumentException("Inverter rated power must be > 0 kW, got " + ratedPowerKw);
        }
        if (efficiency <= 0.0 || efficiency > 1.0) {
            throw new IllegalArgumentException("Inverter efficiency must be in (0, 1.0], got " + efficiency);
        }

        this.ratedPowerKw = ratedPowerKw;
        this.maxChargeLimitKw = (maxChargeLimitKw != null) ? maxChargeLimitKw : ratedPowerKw;
        this.maxDischargeLimitKw = (maxDischargeLimitKw != null) ? maxDischargeLimitKw : ratedPowerKw;

        if (this.maxChargeLimitKw < 0.0) {
            throw new IllegalArgumentException("Inverter max charge limit must be non-negative");
        }
        if (this.maxDischargeLimitKw < 0.0) {
            throw new IllegalArgumentException("Inverter max discharge limit must be non-negative");
        }
        this.efficiency = efficiency;
    }

    public Inverter(double ratedPowerKw) {
        this(ratedPowerKw, null, null, 0.98);
    }

    public boolean canCharge(double powerKw) {
        return powerKw >= 0.0 && powerKw <= maxChargeLimitKw;
    }

    public boolean canDischarge(double powerKw) {
        return powerKw >= 0.0 && powerKw <= maxDischargeLimitKw;
    }

    public double clampChargePower(double powerKw) {
        if (powerKw < 0.0) {
            return 0.0;
        }
        return Math.min(powerKw, maxChargeLimitKw);
    }

    public double clampDischargePower(double powerKw) {
        if (powerKw < 0.0) {
            return 0.0;
        }
        return Math.min(powerKw, maxDischargeLimitKw);
    }

    public double getRatedPowerKw() {
        return ratedPowerKw;
    }

    public double getMaxChargeLimitKw() {
        return maxChargeLimitKw;
    }

    public double getMaxDischargeLimitKw() {
        return maxDischargeLimitKw;
    }

    public double getEfficiency() {
        return efficiency;
    }

    @Override
    public String toString() {
        return "Inverter{ratedPowerKw=" + ratedPowerKw + ", maxCharge=" + maxChargeLimitKw
                + " kW, maxDischarge=" + maxDischargeLimitKw + " kW, efficiency=" + efficiency + "}";
    }
}

