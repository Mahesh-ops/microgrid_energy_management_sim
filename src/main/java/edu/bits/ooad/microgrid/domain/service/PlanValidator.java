package edu.bits.ooad.microgrid.domain.service;

import edu.bits.ooad.microgrid.domain.model.MeasurementSnapshot;
import edu.bits.ooad.microgrid.domain.model.PowerPlan;
import edu.bits.ooad.microgrid.domain.state.GridState;

import java.util.ArrayList;
import java.util.List;

/**
 * PlanValidator enforces physical constraints, equipment limits, and power balance
 * rules before any proposed power dispatch plan is applied to simulated components.
 */
public class PlanValidator {

    private static final double TOLERANCE = 1e-4;

    /**
     * Validates and safeguards a proposed PowerPlan against component ratings and grid state.
     *
     * @param plan Proposed plan from an EnergyManagementStrategy
     * @param snapshot Current measurement snapshot
     * @param gridState Current active grid state
     * @return ValidationResult containing validated (safely clamped) plan and audit messages
     */
    public ValidationResult validate(PowerPlan plan, MeasurementSnapshot snapshot, GridState gridState) {
        List<String> auditLogs = new ArrayList<>(plan.getDecisionLogs());
        boolean initiallyValid = true;

        double solarToLoad = plan.getSolarToLoadKw();
        double solarToBattery = plan.getSolarToBatteryKw();
        double solarToGrid = plan.getSolarToGridKw();
        double batteryDischarge = plan.getBatteryDischargeToLoadKw();
        double gridToLoad = plan.getGridToLoadKw();
        double curtailedSolar = plan.getCurtailedSolarKw();
        double unmetCritical = plan.getUnmetCriticalLoadKw();
        double unmetNonCritical = plan.getUnmetNonCriticalLoadKw();

        // 1. Simultaneous charge and discharge prevention
        if (solarToBattery > TOLERANCE && batteryDischarge > TOLERANCE) {
            initiallyValid = false;
            auditLogs.add("SAFETY VIOLATION: Simultaneous charge and discharge detected; cancelling discharge");
            batteryDischarge = 0.0;
        }

        // 2. Solar generation conservation
        double totalSolarAllocated = solarToLoad + solarToBattery + solarToGrid;
        if (totalSolarAllocated > snapshot.solarGenerationKw() + TOLERANCE) {
            initiallyValid = false;
            auditLogs.add(String.format("SAFETY CLAMP: Total solar allocated (%.2f kW) exceeded available (%.2f kW)",
                    totalSolarAllocated, snapshot.solarGenerationKw()));
            // Scale down allocations proportionally
            double scale = snapshot.solarGenerationKw() / totalSolarAllocated;
            solarToLoad *= scale;
            solarToBattery *= scale;
            solarToGrid *= scale;
        }

        // Recalculate curtailment
        double usedSolar = solarToLoad + solarToBattery + solarToGrid;
        curtailedSolar = Math.max(0.0, snapshot.solarGenerationKw() - usedSolar);

        // 3. Battery charging limits
        double maxFeasibleCharge = Math.min(snapshot.batteryFeasibleChargeKw(), snapshot.inverterMaxChargeKw());
        if (solarToBattery > maxFeasibleCharge + TOLERANCE) {
            initiallyValid = false;
            auditLogs.add(String.format("SAFETY CLAMP: Battery charge request (%.2f kW) clamped to limit (%.2f kW)",
                    solarToBattery, maxFeasibleCharge));
            double excess = solarToBattery - maxFeasibleCharge;
            solarToBattery = maxFeasibleCharge;
            curtailedSolar += excess;
        }

        // 4. Battery discharging limits
        double maxFeasibleDischarge = Math.min(snapshot.batteryFeasibleDischargeKw(), snapshot.inverterMaxDischargeKw());
        if (batteryDischarge > maxFeasibleDischarge + TOLERANCE) {
            initiallyValid = false;
            auditLogs.add(String.format("SAFETY CLAMP: Battery discharge request (%.2f kW) clamped to limit (%.2f kW)",
                    batteryDischarge, maxFeasibleDischarge));
            batteryDischarge = maxFeasibleDischarge;
        }

        // 5. Grid export limits & Islanded isolation
        if (!gridState.canExport() && solarToGrid > TOLERANCE) {
            initiallyValid = false;
            auditLogs.add("ISLANDING ENFORCEMENT: Grid export blocked in Islanded state; diverted to curtailment");
            curtailedSolar += solarToGrid;
            solarToGrid = 0.0;
        } else if (solarToGrid > snapshot.gridMaxExportKw() + TOLERANCE) {
            initiallyValid = false;
            auditLogs.add(String.format("SAFETY CLAMP: Grid export (%.2f kW) clamped to feeder limit (%.2f kW)",
                    solarToGrid, snapshot.gridMaxExportKw()));
            double excess = solarToGrid - snapshot.gridMaxExportKw();
            solarToGrid = snapshot.gridMaxExportKw();
            curtailedSolar += excess;
        }

        // 6. Grid import limits & Islanded isolation
        if (!gridState.canImport() && gridToLoad > TOLERANCE) {
            initiallyValid = false;
            auditLogs.add("ISLANDING ENFORCEMENT: Grid import blocked in Islanded state");
            gridToLoad = 0.0;
        } else if (gridToLoad > snapshot.gridMaxImportKw() + TOLERANCE) {
            initiallyValid = false;
            auditLogs.add(String.format("SAFETY CLAMP: Grid import (%.2f kW) clamped to feeder limit (%.2f kW)",
                    gridToLoad, snapshot.gridMaxImportKw()));
            gridToLoad = snapshot.gridMaxImportKw();
        }

        // 7. Load balance reconciliation
        double totalSupplied = solarToLoad + batteryDischarge + gridToLoad;
        double loadDemand = snapshot.totalDemandKw();

        if (totalSupplied > loadDemand + TOLERANCE) {
            initiallyValid = false;
            double oversupply = totalSupplied - loadDemand;
            auditLogs.add(String.format("SAFETY CLAMP: Oversupply of %.2f kW detected; reducing grid/battery supply", oversupply));
            // Reduce grid first, then battery
            if (gridToLoad >= oversupply) {
                gridToLoad -= oversupply;
            } else {
                oversupply -= gridToLoad;
                gridToLoad = 0.0;
                batteryDischarge = Math.max(0.0, batteryDischarge - oversupply);
            }
            totalSupplied = solarToLoad + batteryDischarge + gridToLoad;
        }

        // Recompute unmet loads with critical load priority
        double criticalDemand = snapshot.criticalDemandKw();
        double nonCriticalDemand = snapshot.nonCriticalDemandKw();

        if (totalSupplied < criticalDemand) {
            unmetCritical = criticalDemand - totalSupplied;
            unmetNonCritical = nonCriticalDemand;
        } else {
            unmetCritical = 0.0;
            unmetNonCritical = Math.max(0.0, loadDemand - totalSupplied);
        }

        PowerPlan validatedPlan = new PowerPlan(
                solarToLoad,
                solarToBattery,
                solarToGrid,
                batteryDischarge,
                gridToLoad,
                curtailedSolar,
                unmetCritical,
                unmetNonCritical,
                auditLogs);

        return new ValidationResult(initiallyValid, validatedPlan, auditLogs);
    }
}

