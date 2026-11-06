package edu.bits.ooad.microgrid.domain.factory;

import edu.bits.ooad.microgrid.domain.model.Battery;
import edu.bits.ooad.microgrid.domain.model.LeadAcidBattery;
import edu.bits.ooad.microgrid.domain.model.LithiumIonBattery;
import edu.bits.ooad.microgrid.domain.model.SodiumNickelBattery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatteryFactoryTest {

    private final BatteryFactory factory = new BatteryFactory();

    @Test
    @DisplayName("Factory should return LithiumIonBattery with correct defaults")
    void testCreateLithiumIon() {
        Battery battery = factory.createBattery(BatteryType.LITHIUM_ION, 100.0, 50.0);
        assertInstanceOf(LithiumIonBattery.class, battery);
        assertEquals(LithiumIonBattery.TECHNOLOGY, battery.getTechnologyName());
        assertEquals(15.0, battery.getMinSocPercent());
        assertEquals(95.0, battery.getMaxSocPercent());
        assertEquals(80.0, battery.getMaxChargePowerKw());
    }

    @Test
    @DisplayName("Factory should return LeadAcidBattery with correct defaults")
    void testCreateLeadAcid() {
        Battery battery = factory.createBattery(BatteryType.LEAD_ACID, 100.0, 60.0);
        assertInstanceOf(LeadAcidBattery.class, battery);
        assertEquals(LeadAcidBattery.TECHNOLOGY, battery.getTechnologyName());
        assertEquals(40.0, battery.getMinSocPercent());
        assertEquals(30.0, battery.getMaxChargePowerKw());
    }

    @Test
    @DisplayName("Factory should return SodiumNickelBattery with correct defaults")
    void testCreateSodiumNickel() {
        Battery battery = factory.createBattery(BatteryType.SODIUM_NICKEL, 100.0, 50.0);
        assertInstanceOf(SodiumNickelBattery.class, battery);
        assertEquals(SodiumNickelBattery.TECHNOLOGY, battery.getTechnologyName());
        assertEquals(20.0, battery.getMinSocPercent());
        assertEquals(50.0, battery.getMaxChargePowerKw());
    }

    @Test
    @DisplayName("Factory should support custom configuration overrides")
    void testCreateCustomBattery() {
        BatteryConfiguration config = new BatteryConfiguration(
                "Custom LiFePO4",
                120.0,
                70.0,
                10.0,
                98.0,
                60.0,
                60.0,
                0.97,
                0.97);

        Battery custom = factory.createCustomBattery(BatteryType.LITHIUM_ION, config);
        assertEquals("Custom LiFePO4", custom.getName());
        assertEquals(120.0, custom.getCapacityKwh());
        assertEquals(70.0, custom.getSocPercent());
        assertEquals(10.0, custom.getMinSocPercent());
        assertEquals(98.0, custom.getMaxSocPercent());
        assertEquals(0.97, custom.getChargeEfficiency());
    }
}

