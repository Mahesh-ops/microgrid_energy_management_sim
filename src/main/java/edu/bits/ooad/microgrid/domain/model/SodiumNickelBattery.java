package edu.bits.ooad.microgrid.domain.model;

/**
 * Concrete implementation for Sodium-Nickel Chloride (NaNiCl2 / ZEBRA) battery technology.
 * High-temperature molten salt battery characterized by 88% round-trip efficiency,
 * robust cycle life, and intermediate SOC operating limits (20-90%).
 */
public class SodiumNickelBattery extends Battery {

    public static final String TECHNOLOGY = "Sodium-Nickel (ZEBRA)";
    public static final double DEFAULT_MIN_SOC = 20.0;
    public static final double DEFAULT_MAX_SOC = 90.0;
    public static final double DEFAULT_EFFICIENCY = 0.88;

    public SodiumNickelBattery(
            String name,
            double capacityKwh,
            double socPercent,
            double minSocPercent,
            double maxSocPercent,
            double maxChargePowerKw,
            double maxDischargePowerKw,
            double chargeEfficiency,
            double dischargeEfficiency) {
        super(
                name,
                capacityKwh,
                socPercent,
                minSocPercent,
                maxSocPercent,
                maxChargePowerKw,
                maxDischargePowerKw,
                chargeEfficiency,
                dischargeEfficiency);
    }

    public static SodiumNickelBattery createDefault(double capacityKwh, double socPercent) {
        double power = capacityKwh * 0.5;
        return new SodiumNickelBattery(
                "Sodium-Nickel Bank",
                capacityKwh,
                socPercent,
                DEFAULT_MIN_SOC,
                DEFAULT_MAX_SOC,
                power,
                power,
                DEFAULT_EFFICIENCY,
                DEFAULT_EFFICIENCY);
    }

    @Override
    public String getTechnologyName() {
        return TECHNOLOGY;
    }
}

