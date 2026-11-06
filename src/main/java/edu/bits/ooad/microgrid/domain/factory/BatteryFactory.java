package edu.bits.ooad.microgrid.domain.factory;

import edu.bits.ooad.microgrid.domain.model.Battery;
import edu.bits.ooad.microgrid.domain.model.LeadAcidBattery;
import edu.bits.ooad.microgrid.domain.model.LithiumIonBattery;
import edu.bits.ooad.microgrid.domain.model.SodiumNickelBattery;

/**
 * Factory class for instantiating Battery implementations based on requested chemistry.
 * Implements the Factory Method pattern to decouple client code from concrete battery models.
 */
public class BatteryFactory {

    /**
     * Creates a battery with chemistry-specific default ratings and limits.
     *
     * @param type         Battery technology type
     * @param capacityKwh  Nominal capacity in kWh
     * @param initialSoc   Initial state of charge in percent
     * @return Fully configured concrete Battery instance
     */
    public Battery createBattery(BatteryType type, double capacityKwh, double initialSoc) {
        if (type == null) {
            type = BatteryType.LITHIUM_ION;
        }

        return switch (type) {
            case LITHIUM_ION -> LithiumIonBattery.createDefault(capacityKwh, initialSoc);
            case LEAD_ACID -> LeadAcidBattery.createDefault(capacityKwh, initialSoc);
            case SODIUM_NICKEL -> SodiumNickelBattery.createDefault(capacityKwh, initialSoc);
        };
    }

    /**
     * Creates a battery with custom user overrides, applying defaults for unspecified fields.
     */
    public Battery createCustomBattery(BatteryType type, BatteryConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Battery configuration must not be null");
        }
        if (type == null) {
            type = BatteryType.LITHIUM_ION;
        }

        String name = (config.name() != null && !config.name().isBlank()) ? config.name() : type.getDisplayName();
        double capacity = config.capacityKwh();
        double soc = config.initialSocPercent();

        return switch (type) {
            case LITHIUM_ION -> {
                double minSoc = (config.minSocPercent() != null) ? config.minSocPercent() : LithiumIonBattery.DEFAULT_MIN_SOC;
                double maxSoc = (config.maxSocPercent() != null) ? config.maxSocPercent() : LithiumIonBattery.DEFAULT_MAX_SOC;
                double defaultPower = capacity * 0.8;
                double maxCharge = (config.maxChargePowerKw() != null) ? config.maxChargePowerKw() : defaultPower;
                double maxDischarge = (config.maxDischargePowerKw() != null) ? config.maxDischargePowerKw() : defaultPower;
                double chargeEff = (config.chargeEfficiency() != null) ? config.chargeEfficiency() : LithiumIonBattery.DEFAULT_EFFICIENCY;
                double dischargeEff = (config.dischargeEfficiency() != null) ? config.dischargeEfficiency() : LithiumIonBattery.DEFAULT_EFFICIENCY;

                yield new LithiumIonBattery(name, capacity, soc, minSoc, maxSoc, maxCharge, maxDischarge, chargeEff, dischargeEff);
            }
            case LEAD_ACID -> {
                double minSoc = (config.minSocPercent() != null) ? config.minSocPercent() : LeadAcidBattery.DEFAULT_MIN_SOC;
                double maxSoc = (config.maxSocPercent() != null) ? config.maxSocPercent() : LeadAcidBattery.DEFAULT_MAX_SOC;
                double defaultPower = capacity * 0.3;
                double maxCharge = (config.maxChargePowerKw() != null) ? config.maxChargePowerKw() : defaultPower;
                double maxDischarge = (config.maxDischargePowerKw() != null) ? config.maxDischargePowerKw() : defaultPower;
                double chargeEff = (config.chargeEfficiency() != null) ? config.chargeEfficiency() : LeadAcidBattery.DEFAULT_EFFICIENCY;
                double dischargeEff = (config.dischargeEfficiency() != null) ? config.dischargeEfficiency() : LeadAcidBattery.DEFAULT_EFFICIENCY;

                yield new LeadAcidBattery(name, capacity, soc, minSoc, maxSoc, maxCharge, maxDischarge, chargeEff, dischargeEff);
            }
            case SODIUM_NICKEL -> {
                double minSoc = (config.minSocPercent() != null) ? config.minSocPercent() : SodiumNickelBattery.DEFAULT_MIN_SOC;
                double maxSoc = (config.maxSocPercent() != null) ? config.maxSocPercent() : SodiumNickelBattery.DEFAULT_MAX_SOC;
                double defaultPower = capacity * 0.5;
                double maxCharge = (config.maxChargePowerKw() != null) ? config.maxChargePowerKw() : defaultPower;
                double maxDischarge = (config.maxDischargePowerKw() != null) ? config.maxDischargePowerKw() : defaultPower;
                double chargeEff = (config.chargeEfficiency() != null) ? config.chargeEfficiency() : SodiumNickelBattery.DEFAULT_EFFICIENCY;
                double dischargeEff = (config.dischargeEfficiency() != null) ? config.dischargeEfficiency() : SodiumNickelBattery.DEFAULT_EFFICIENCY;

                yield new SodiumNickelBattery(name, capacity, soc, minSoc, maxSoc, maxCharge, maxDischarge, chargeEff, dischargeEff);
            }
        };
    }
}

