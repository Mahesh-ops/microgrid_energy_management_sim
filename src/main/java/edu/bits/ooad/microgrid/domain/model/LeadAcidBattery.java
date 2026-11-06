package edu.bits.ooad.microgrid.domain.model;

/**
 * Concrete implementation for Lead-Acid battery technology (VRLA / AGM).
 * Characterized by moderate round-trip efficiency (80-82%), restricted depth of discharge
 * to prevent sulfation (min SOC 40%), and moderate power limits (~0.3C).
 */
public class LeadAcidBattery extends Battery {

    public static final String TECHNOLOGY = "Lead-Acid (VRLA)";
    public static final double DEFAULT_MIN_SOC = 40.0;
    public static final double DEFAULT_MAX_SOC = 95.0;
    public static final double DEFAULT_EFFICIENCY = 0.82;

    public LeadAcidBattery(
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

    public static LeadAcidBattery createDefault(double capacityKwh, double socPercent) {
        double power = capacityKwh * 0.3;
        return new LeadAcidBattery(
                "Lead-Acid Bank",
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

