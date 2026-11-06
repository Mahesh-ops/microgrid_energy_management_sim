package edu.bits.ooad.microgrid.domain.model;

/**
 * Concrete implementation for Lithium-Ion battery technology (e.g. LiFePO4 or NMC).
 * Characterized by high round-trip efficiency (92-95%), wide usable SOC window (15-95%),
 * and high continuous power capability (0.8C - 1.0C).
 */
public class LithiumIonBattery extends Battery {

    public static final String TECHNOLOGY = "Lithium-Ion (LiFePO4)";
    public static final double DEFAULT_MIN_SOC = 15.0;
    public static final double DEFAULT_MAX_SOC = 95.0;
    public static final double DEFAULT_EFFICIENCY = 0.95;

    public LithiumIonBattery(
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

    public static LithiumIonBattery createDefault(double capacityKwh, double socPercent) {
        double power = capacityKwh * 0.8;
        return new LithiumIonBattery(
                "Lithium-Ion Bank",
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

