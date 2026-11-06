package edu.bits.ooad.microgrid.domain.factory;

/**
 * Optional parameter carrier for constructing customized battery instances.
 */
public record BatteryConfiguration(
        String name,
        double capacityKwh,
        double initialSocPercent,
        Double minSocPercent,
        Double maxSocPercent,
        Double maxChargePowerKw,
        Double maxDischargePowerKw,
        Double chargeEfficiency,
        Double dischargeEfficiency) {

    public static BatteryConfiguration standard(String name, double capacityKwh, double initialSocPercent) {
        return new BatteryConfiguration(
                name, capacityKwh, initialSocPercent, null, null, null, null, null, null);
    }
}

