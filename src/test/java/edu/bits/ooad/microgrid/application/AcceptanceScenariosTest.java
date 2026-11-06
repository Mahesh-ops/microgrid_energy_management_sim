package edu.bits.ooad.microgrid.application;

import edu.bits.ooad.microgrid.domain.factory.BatteryFactory;
import edu.bits.ooad.microgrid.domain.factory.BatteryType;
import edu.bits.ooad.microgrid.domain.model.*;
import edu.bits.ooad.microgrid.domain.service.MonitoringService;
import edu.bits.ooad.microgrid.domain.service.PlanValidator;
import edu.bits.ooad.microgrid.domain.state.GridConnectedState;
import edu.bits.ooad.microgrid.domain.state.GridStateType;
import edu.bits.ooad.microgrid.domain.strategy.EnergyManagementStrategy;
import edu.bits.ooad.microgrid.domain.strategy.PeakShavingStrategy;
import edu.bits.ooad.microgrid.domain.strategy.SelfConsumptionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the 12 primary acceptance scenarios (T1 - T12) defined in plan.md.
 */
class AcceptanceScenariosTest {

    private SolarPV solarPV;
    private Load load;
    private Battery battery;
    private Inverter inverter;
    private UtilityGrid utilityGrid;
    private MicrogridController controller;
    private MonitoringService monitoring;
    private SimulationEngine engine;
    private BatteryFactory batteryFactory;

    @BeforeEach
    void setUp() {
        batteryFactory = new BatteryFactory();
        solarPV = new SolarPV(100.0, 50.0);
        load = new Load(50.0, 20.0);
        battery = batteryFactory.createBattery(BatteryType.LITHIUM_ION, 100.0, 50.0);
        inverter = new Inverter(60.0);
        utilityGrid = new UtilityGrid(true, 100.0, 50.0);

        controller = new MicrogridController(
                solarPV, load, battery, inverter, utilityGrid,
                new SelfConsumptionStrategy(),
                new GridConnectedState(),
                new PlanValidator());

        monitoring = new MonitoringService();
        engine = new SimulationEngine(controller, monitoring);
    }

    @Test
    @DisplayName("T1: Solar generation equals load demand")
    void testT1_SolarEqualsLoad() {
        controller.updateInputs(50.0, 50.0, 20.0);
        SimulationResult result = engine.runStep();

        assertEquals(50.0, result.solarToLoadKw());
        assertEquals(0.0, result.batteryDischargeKw());
        assertEquals(0.0, result.batteryChargeKw());
        assertEquals(0.0, result.gridImportKw());
        assertEquals(0.0, result.gridExportKw());
        assertEquals(50.0, result.servedLoadKw());
        assertEquals(0.0, result.unmetLoadKw());
    }

    @Test
    @DisplayName("T2: Solar exceeds load demand")
    void testT2_SolarExceedsLoad() {
        controller.updateInputs(80.0, 30.0, 10.0);
        SimulationResult result = engine.runStep();

        assertEquals(30.0, result.solarToLoadKw());
        assertTrue(result.solarToBatteryKw() > 0.0, "Battery should absorb surplus solar");
        assertTrue(result.batteryChargeKw() > 0.0);
        assertEquals(0.0, result.batteryDischargeKw());
        assertEquals(0.0, result.gridImportKw());
        assertEquals(0.0, result.unmetLoadKw());
    }

    @Test
    @DisplayName("T3: Load exceeds solar generation")
    void testT3_LoadExceedsSolar() {
        controller.updateInputs(20.0, 70.0, 20.0);
        SimulationResult result = engine.runStep();

        assertEquals(20.0, result.solarToLoadKw());
        assertTrue(result.batteryDischargeKw() > 0.0, "Battery should discharge to cover deficit");
        assertEquals(0.0, result.batteryChargeKw());
        assertEquals(70.0, result.servedLoadKw());
        assertEquals(0.0, result.unmetLoadKw());
    }

    @Test
    @DisplayName("T4: Battery at minimum SOC prevents further discharge")
    void testT4_BatteryAtMinSoc() {
        // Set battery directly to minimum SOC (15% for Li-Ion)
        battery.setSocPercent(battery.getMinSocPercent());
        controller.updateInputs(10.0, 60.0, 20.0);

        SimulationResult result = engine.runStep();
        assertEquals(0.0, result.batteryDischargeKw(), "Discharge must be 0 when at min SOC");
        assertEquals(50.0, result.gridImportKw(), "Grid must supply remaining deficit");
        assertEquals(battery.getMinSocPercent(), result.finalBatterySocPercent(), 1e-2);
    }

    @Test
    @DisplayName("T5: Battery at maximum SOC prevents further charging")
    void testT5_BatteryAtMaxSoc() {
        // Set battery directly to maximum SOC (95% for Li-Ion)
        battery.setSocPercent(battery.getMaxSocPercent());
        controller.updateInputs(80.0, 20.0, 10.0);

        SimulationResult result = engine.runStep();
        assertEquals(0.0, result.batteryChargeKw(), "Charging must be 0 when at max SOC");
        assertTrue(result.solarToGridKw() > 0.0, "Surplus solar should export to grid");
        assertEquals(battery.getMaxSocPercent(), result.finalBatterySocPercent(), 1e-2);
    }

    @Test
    @DisplayName("T6: Requested power clamped to inverter rating")
    void testT6_InverterLimitEnforcement() {
        // Create an inverter with a tight 15 kW limit
        Inverter tightInverter = new Inverter(15.0);
        MicrogridController testController = new MicrogridController(
                solarPV, load, battery, tightInverter, utilityGrid,
                new SelfConsumptionStrategy(), new GridConnectedState(), new PlanValidator());
        SimulationEngine testEngine = new SimulationEngine(testController, new MonitoringService());

        // Demand deficit = 60 kW (solar 10, load 70)
        testController.updateInputs(10.0, 70.0, 20.0);
        SimulationResult result = testEngine.runStep();

        assertTrue(result.batteryDischargeKw() <= 15.0, "Discharge must be clamped to inverter limit of 15 kW");
    }

    @Test
    @DisplayName("T7: Grid becomes unavailable -> Islanded mode with zero grid exchange")
    void testT7_GridOutage() {
        controller.simulateGridFailure();
        assertEquals(GridStateType.ISLANDED, controller.getGridState().getStateType());

        controller.updateInputs(20.0, 50.0, 20.0);
        SimulationResult result = engine.runStep();

        assertEquals(0.0, result.gridImportKw(), "Grid import must be 0 in islanded mode");
        assertEquals(0.0, result.gridExportKw(), "Grid export must be 0 in islanded mode");
    }

    @Test
    @DisplayName("T8: Islanded supply shortage prioritizes critical load")
    void testT8_IslandedShortageCriticalLoadPriority() {
        controller.simulateGridFailure();
        battery.setSocPercent(battery.getMinSocPercent()); // Battery cannot discharge

        // Solar: 25 kW, Total Load: 60 kW (Critical: 30 kW)
        controller.updateInputs(25.0, 60.0, 30.0);
        SimulationResult result = engine.runStep();

        assertEquals(25.0, result.solarToLoadKw());
        assertEquals(0.0, result.batteryDischargeKw());
        assertEquals(0.0, result.gridImportKw());
        assertEquals(25.0, result.servedLoadKw());
        assertEquals(35.0, result.unmetLoadKw());
        // Critical was 30 kW, solar gave 25 kW -> 5 kW critical unmet, all 30 kW non-critical unmet
        assertEquals(5.0, result.unmetCriticalLoadKw());
    }

    @Test
    @DisplayName("T9: Grid restoration requires simulated reconnection check")
    void testT9_GridRestoration() {
        controller.simulateGridFailure();
        assertEquals(GridStateType.ISLANDED, controller.getGridState().getStateType());

        // Calling simulateGridRestore triggers reconnection verification
        boolean reconnected = controller.simulateGridRestore();
        assertTrue(reconnected);
        assertEquals(GridStateType.GRID_CONNECTED, controller.getGridState().getStateType());
    }

    @Test
    @DisplayName("T10: Strategy changed polymorphically without rewriting controller")
    void testT10_StrategyChanged() {
        controller.setStrategy(new SelfConsumptionStrategy());
        assertEquals("Self-Consumption", controller.getStrategy().getStrategyName());

        controller.setStrategy(new PeakShavingStrategy(20.0));
        controller.setPeakShavingTargetKw(20.0);
        assertEquals("Peak Shaving", controller.getStrategy().getStrategyName());

        // Peak shaving with demand 45 kW, solar 0 kW, target 20 kW
        controller.updateInputs(0.0, 45.0, 15.0);
        SimulationResult result = engine.runStep();

        assertEquals("Peak Shaving", result.strategyName());
        assertEquals(25.0, result.batteryDischargeKw(), "Battery should shave 25 kW above target");
        assertEquals(20.0, result.gridImportKw(), "Grid should supply target 20 kW");
    }

    @Test
    @DisplayName("T11: Battery technology changed via BatteryFactory behind common interface")
    void testT11_BatteryTechnologySwitch() {
        Battery leadAcid = batteryFactory.createBattery(BatteryType.LEAD_ACID, 100.0, 60.0);
        controller.setBattery(leadAcid);

        assertEquals(LeadAcidBattery.TECHNOLOGY, controller.getBattery().getTechnologyName());
        assertEquals(40.0, controller.getBattery().getMinSocPercent());

        Battery sodiumNickel = batteryFactory.createBattery(BatteryType.SODIUM_NICKEL, 100.0, 50.0);
        controller.setBattery(sodiumNickel);

        assertEquals(SodiumNickelBattery.TECHNOLOGY, controller.getBattery().getTechnologyName());
        assertEquals(20.0, controller.getBattery().getMinSocPercent());
    }

    @Test
    @DisplayName("T12: Multi-step run records history and generates valid CSV export")
    void testT12_MultiStepMonitoringAndCsvExport() {
        monitoring.clear();
        controller.updateInputs(60.0, 40.0, 15.0);
        List<SimulationResult> results = engine.runSteps(5);

        assertEquals(5, results.size());
        assertEquals(5, monitoring.getStepCount());
        assertEquals(5, monitoring.getHistory().size());

        String csv = monitoring.exportCsv();
        assertNotNull(csv);
        String[] lines = csv.trim().split("\n");
        assertEquals(6, lines.length, "1 header row + 5 data rows expected");
        assertTrue(lines[0].startsWith("Step,State,Strategy"));
    }
}
