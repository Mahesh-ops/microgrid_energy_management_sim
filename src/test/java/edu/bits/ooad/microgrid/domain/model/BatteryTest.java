package edu.bits.ooad.microgrid.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatteryTest {

    @Test
    @DisplayName("Should correctly calculate charge delta SOC with efficiency")
    void testBatteryCharging() {
        LithiumIonBattery battery = LithiumIonBattery.createDefault(100.0, 50.0);
        // 100 kWh, 50% SOC, 95% charge efficiency, step 1 hr
        // 10 kW * 1 hr * 0.95 = 9.5 kWh stored -> +9.5%
        double charged = battery.charge(10.0, 1.0);
        assertEquals(10.0, charged, 1e-4);
        assertEquals(59.5, battery.getSocPercent(), 1e-2);
    }

    @Test
    @DisplayName("Should correctly calculate discharge delta SOC with efficiency")
    void testBatteryDischarging() {
        LithiumIonBattery battery = LithiumIonBattery.createDefault(100.0, 50.0);
        // 100 kWh, 50% SOC, 95% discharge efficiency, step 1 hr
        // 10 kW * 1 hr / 0.95 = 10.526 kWh chemical energy consumed -> -10.526%
        double discharged = battery.discharge(10.0, 1.0);
        assertEquals(10.0, discharged, 1e-4);
        assertEquals(50.0 - (10.0 / 95.0 * 100.0), battery.getSocPercent(), 1e-2);
    }

    @Test
    @DisplayName("Should prevent charging beyond max SOC limit")
    void testChargeClampingAtMaxSoc() {
        LithiumIonBattery battery = LithiumIonBattery.createDefault(100.0, 94.0);
        // Max SOC is 95%. Only 1% room = 1.0 kWh chemical.
        // Required charging power = 1.0 / (1.0 * 0.95) = 1.0526 kW
        double maxChargeKw = battery.calculateMaxChargePower(1.0);
        assertTrue(maxChargeKw < 1.1);

        double actualCharged = battery.charge(50.0, 1.0);
        assertEquals(maxChargeKw, actualCharged, 1e-3);
        assertEquals(95.0, battery.getSocPercent(), 1e-2);

        // Charging again returns 0
        double furtherCharge = battery.charge(10.0, 1.0);
        assertEquals(0.0, furtherCharge, 1e-4);
        assertEquals(95.0, battery.getSocPercent(), 1e-2);
    }

    @Test
    @DisplayName("Should prevent discharging below min SOC limit")
    void testDischargeClampingAtMinSoc() {
        LeadAcidBattery battery = LeadAcidBattery.createDefault(100.0, 42.0);
        // Lead-Acid min SOC is 40%. Only 2% available = 2.0 kWh chemical.
        // Deliverable power = 2.0 * 0.82 / 1.0 = 1.64 kW
        double maxDischargeKw = battery.calculateMaxDischargePower(1.0);
        assertEquals(1.64, maxDischargeKw, 1e-2);

        double actualDischarged = battery.discharge(20.0, 1.0);
        assertEquals(maxDischargeKw, actualDischarged, 1e-2);
        assertEquals(40.0, battery.getSocPercent(), 1e-2);

        // Discharging again returns 0
        double furtherDischarge = battery.discharge(10.0, 1.0);
        assertEquals(0.0, furtherDischarge, 1e-4);
        assertEquals(40.0, battery.getSocPercent(), 1e-2);
    }

    @Test
    @DisplayName("Should reject invalid battery constructor arguments")
    void testInvalidBatteryParameters() {
        assertThrows(IllegalArgumentException.class, () -> new LithiumIonBattery(
                "Bad", -10.0, 50.0, 15.0, 95.0, 50.0, 50.0, 0.95, 0.95));

        assertThrows(IllegalArgumentException.class, () -> new LithiumIonBattery(
                "Bad", 100.0, 50.0, 95.0, 15.0, 50.0, 50.0, 0.95, 0.95));

        assertThrows(IllegalArgumentException.class, () -> new LithiumIonBattery(
                "Bad", 100.0, 50.0, 15.0, 95.0, 50.0, 50.0, 1.5, 0.95));
    }

    @Test
    @DisplayName("Should verify specific chemistry technology names and defaults")
    void testChemistryDefaults() {
        Battery li = LithiumIonBattery.createDefault(100.0, 50.0);
        assertEquals(LithiumIonBattery.TECHNOLOGY, li.getTechnologyName());
        assertEquals(15.0, li.getMinSocPercent());

        Battery lead = LeadAcidBattery.createDefault(100.0, 50.0);
        assertEquals(LeadAcidBattery.TECHNOLOGY, lead.getTechnologyName());
        assertEquals(40.0, lead.getMinSocPercent());

        Battery na = SodiumNickelBattery.createDefault(100.0, 50.0);
        assertEquals(SodiumNickelBattery.TECHNOLOGY, na.getTechnologyName());
        assertEquals(20.0, na.getMinSocPercent());
    }
}

