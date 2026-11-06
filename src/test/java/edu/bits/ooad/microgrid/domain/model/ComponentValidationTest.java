package edu.bits.ooad.microgrid.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComponentValidationTest {

    @Test
    @DisplayName("SolarPV should validate capacity and generation limits")
    void testSolarPVValidation() {
        SolarPV solar = new SolarPV(100.0, 50.0);
        assertEquals(50.0, solar.getAvailablePowerKw());

        solar.setGeneration(80.0);
        assertEquals(80.0, solar.getAvailablePowerKw());

        assertThrows(IllegalArgumentException.class, () -> new SolarPV(-10.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new SolarPV(50.0, 60.0));
        assertThrows(IllegalArgumentException.class, () -> solar.setGeneration(-5.0));
        assertThrows(IllegalArgumentException.class, () -> solar.setGeneration(120.0));
    }

    @Test
    @DisplayName("Load should validate total and critical demand")
    void testLoadValidation() {
        Load load = new Load(100.0, 30.0);
        assertEquals(100.0, load.getTotalDemandKw());
        assertEquals(30.0, load.getCriticalDemandKw());
        assertEquals(70.0, load.getNonCriticalDemandKw());

        load.update(80.0, 20.0);
        assertEquals(60.0, load.getNonCriticalDemandKw());

        assertThrows(IllegalArgumentException.class, () -> new Load(-10.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Load(50.0, 60.0));
        assertThrows(IllegalArgumentException.class, () -> new Load(50.0, -5.0));
    }

    @Test
    @DisplayName("Inverter should clamp charge and discharge powers to rating")
    void testInverterLimits() {
        Inverter inverter = new Inverter(50.0, 40.0, 45.0, 0.98);
        assertTrue(inverter.canCharge(30.0));
        assertFalse(inverter.canCharge(45.0));
        assertEquals(40.0, inverter.clampChargePower(60.0));
        assertEquals(45.0, inverter.clampDischargePower(60.0));

        assertThrows(IllegalArgumentException.class, () -> new Inverter(0.0));
        assertThrows(IllegalArgumentException.class, () -> new Inverter(50.0, 40.0, 40.0, 1.2));
    }

    @Test
    @DisplayName("UtilityGrid should enforce availability and clamp import/export")
    void testUtilityGridLimits() {
        UtilityGrid grid = new UtilityGrid(true, 100.0, 50.0);
        assertEquals(80.0, grid.clampImport(80.0));
        assertEquals(100.0, grid.clampImport(120.0));
        assertEquals(50.0, grid.clampExport(80.0));

        // When islanded (grid unavailable), import and export must be 0
        grid.setAvailable(false);
        assertFalse(grid.isAvailable());
        assertEquals(0.0, grid.getMaxImportKw());
        assertEquals(0.0, grid.getMaxExportKw());
        assertEquals(0.0, grid.clampImport(50.0));
        assertEquals(0.0, grid.clampExport(50.0));
    }

    @Test
    @DisplayName("PowerPlan should accurately summarize flows and load satisfaction")
    void testPowerPlanCalculations() {
        PowerPlan plan = new PowerPlan(
                40.0, // solarToLoad
                10.0, // solarToBattery
                0.0,  // solarToGrid
                20.0, // batteryDischargeToLoad
                15.0, // gridToLoad
                0.0,  // curtailedSolar
                0.0,  // unmetCritical
                5.0,  // unmetNonCritical
                List.of("Discharge applied", "Grid imported deficit"));

        assertEquals(75.0, plan.getTotalSuppliedLoadKw());
        assertEquals(5.0, plan.getTotalUnmetLoadKw());
        assertEquals(10.0, plan.getBatteryChargeKw());
        assertEquals(20.0, plan.getBatteryDischargeKw());
        assertEquals(15.0, plan.getGridImportKw());
        assertEquals(0.0, plan.getGridExportKw());
    }

    @Test
    @DisplayName("SimulationResult should generate valid CSV representation")
    void testSimulationResultCsv() {
        SimulationResult result = new SimulationResult(
                1, "GridConnected", "Self-Consumption",
                50.0, 60.0, 20.0,
                50.0, 0.0, 0.0,
                10.0, 0.0, 0.0, 0.0,
                60.0, 0.0, 0.0, 0.0,
                50.0, 40.0,
                List.of("Solar covered 50 kW", "Battery discharged 10 kW"));

        String csvRow = result.toCsvRow();
        assertTrue(csvRow.startsWith("1,GridConnected,Self-Consumption,50.00,60.00"));
        assertTrue(csvRow.contains("Solar covered 50 kW; Battery discharged 10 kW"));
    }
}

