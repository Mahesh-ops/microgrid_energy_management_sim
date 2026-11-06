package edu.bits.ooad.microgrid.domain.strategy;

import edu.bits.ooad.microgrid.domain.model.MeasurementSnapshot;
import edu.bits.ooad.microgrid.domain.model.PowerPlan;
import edu.bits.ooad.microgrid.domain.state.GridConnectedState;
import edu.bits.ooad.microgrid.domain.state.GridState;
import edu.bits.ooad.microgrid.domain.state.IslandedState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrategiesTest {

    private final GridState connected = new GridConnectedState();
    private final GridState islanded = new IslandedState();

    @Test
    @DisplayName("SelfConsumption: Solar surplus charges battery and exports remainder")
    void testSelfConsumptionSurplus() {
        EnergyManagementStrategy strategy = new SelfConsumptionStrategy();
        // Solar: 80 kW, Load: 30 kW (Critical: 10 kW). Surplus solar = 50 kW.
        // Battery can charge up to 30 kW. Inverter allows 40 kW.
        // Expected: 30 kW solarToLoad, 30 kW solarToBattery, 20 kW solarToGrid.
        MeasurementSnapshot snapshot = new MeasurementSnapshot(
                80.0, 30.0, 10.0,
                50.0, 15.0, 95.0,
                30.0, 30.0, // feasible charge/discharge
                40.0, 40.0, // inverter limits
                true, 100.0, 50.0,
                1.0, null);

        PowerPlan plan = strategy.calculatePlan(snapshot, connected);
        assertEquals(30.0, plan.getSolarToLoadKw());
        assertEquals(30.0, plan.getSolarToBatteryKw());
        assertEquals(20.0, plan.getSolarToGridKw());
        assertEquals(0.0, plan.getBatteryDischargeToLoadKw());
        assertEquals(0.0, plan.getGridToLoadKw());
        assertEquals(0.0, plan.getTotalUnmetLoadKw());
    }

    @Test
    @DisplayName("SelfConsumption: Islanded mode prevents grid export and curtails surplus")
    void testSelfConsumptionIslandedSurplus() {
        EnergyManagementStrategy strategy = new SelfConsumptionStrategy();
        // Solar: 80 kW, Load: 30 kW. Surplus solar = 50 kW.
        // Battery can charge 30 kW. Remainder 20 kW must be curtailed since Islanded.
        MeasurementSnapshot snapshot = new MeasurementSnapshot(
                80.0, 30.0, 10.0,
                50.0, 15.0, 95.0,
                30.0, 30.0,
                40.0, 40.0,
                false, 0.0, 0.0,
                1.0, null);

        PowerPlan plan = strategy.calculatePlan(snapshot, islanded);
        assertEquals(30.0, plan.getSolarToLoadKw());
        assertEquals(30.0, plan.getSolarToBatteryKw());
        assertEquals(0.0, plan.getSolarToGridKw());
        assertEquals(20.0, plan.getCurtailedSolarKw());
        assertEquals(0.0, plan.getTotalUnmetLoadKw());
    }

    @Test
    @DisplayName("SelfConsumption: Deficit discharges battery and imports shortfall")
    void testSelfConsumptionDeficit() {
        EnergyManagementStrategy strategy = new SelfConsumptionStrategy();
        // Solar: 20 kW, Load: 80 kW. Deficit = 60 kW.
        // Battery can discharge up to 35 kW. Feasible discharge = 35 kW.
        // Grid imports remaining 25 kW.
        MeasurementSnapshot snapshot = new MeasurementSnapshot(
                20.0, 80.0, 20.0,
                50.0, 15.0, 95.0,
                35.0, 35.0,
                50.0, 50.0,
                true, 100.0, 50.0,
                1.0, null);

        PowerPlan plan = strategy.calculatePlan(snapshot, connected);
        assertEquals(20.0, plan.getSolarToLoadKw());
        assertEquals(35.0, plan.getBatteryDischargeToLoadKw());
        assertEquals(25.0, plan.getGridToLoadKw());
        assertEquals(0.0, plan.getTotalUnmetLoadKw());
    }

    @Test
    @DisplayName("PeakShaving: Below peak target, preserves battery and uses grid")
    void testPeakShavingBelowTarget() {
        EnergyManagementStrategy strategy = new PeakShavingStrategy(30.0);
        // Solar: 0 kW, Load: 25 kW. Deficit = 25 kW <= target (30 kW).
        // Battery should NOT discharge; grid supplies 25 kW.
        MeasurementSnapshot snapshot = new MeasurementSnapshot(
                0.0, 25.0, 10.0,
                50.0, 15.0, 95.0,
                40.0, 40.0,
                50.0, 50.0,
                true, 100.0, 50.0,
                1.0, 30.0);

        PowerPlan plan = strategy.calculatePlan(snapshot, connected);
        assertEquals(0.0, plan.getBatteryDischargeToLoadKw());
        assertEquals(25.0, plan.getGridToLoadKw());
        assertEquals(0.0, plan.getTotalUnmetLoadKw());
    }

    @Test
    @DisplayName("PeakShaving: Above peak target, discharges battery to shave spike")
    void testPeakShavingAboveTarget() {
        EnergyManagementStrategy strategy = new PeakShavingStrategy(30.0);
        // Solar: 0 kW, Load: 50 kW. Deficit = 50 kW > target (30 kW).
        // Spike = 20 kW. Battery discharges 20 kW. Grid imports target 30 kW.
        MeasurementSnapshot snapshot = new MeasurementSnapshot(
                0.0, 50.0, 10.0,
                50.0, 15.0, 95.0,
                40.0, 40.0,
                50.0, 50.0,
                true, 100.0, 50.0,
                1.0, 30.0);

        PowerPlan plan = strategy.calculatePlan(snapshot, connected);
        assertEquals(20.0, plan.getBatteryDischargeToLoadKw());
        assertEquals(30.0, plan.getGridToLoadKw());
        assertEquals(0.0, plan.getTotalUnmetLoadKw());
    }
}

