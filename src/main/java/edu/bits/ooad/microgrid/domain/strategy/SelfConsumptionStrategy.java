package edu.bits.ooad.microgrid.domain.strategy;

import edu.bits.ooad.microgrid.domain.model.MeasurementSnapshot;
import edu.bits.ooad.microgrid.domain.model.PowerPlan;
import edu.bits.ooad.microgrid.domain.state.GridState;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete strategy maximizing self-consumption of on-site solar generation.
 * Priority order:
 * 1. Solar generation supplies local load.
 * 2. Surplus solar charges the battery.
 * 3. Remaining surplus is exported to the grid (if grid-connected).
 * 4. In case of solar deficit, battery discharges to cover demand.
 * 5. Remaining deficit is imported from the grid (if grid-connected).
 * 6. In islanded deficit, critical loads are prioritized and unmet demand is recorded.
 */
public class SelfConsumptionStrategy implements EnergyManagementStrategy {

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.SELF_CONSUMPTION;
    }

    @Override
    public String getStrategyName() {
        return "Self-Consumption";
    }

    @Override
    public PowerPlan calculatePlan(MeasurementSnapshot snapshot, GridState gridState) {
        List<String> logs = new ArrayList<>();

        double solarGen = snapshot.solarGenerationKw();
        double totalDemand = snapshot.totalDemandKw();
        double criticalDemand = snapshot.criticalDemandKw();
        double nonCriticalDemand = snapshot.nonCriticalDemandKw();

        double solarToLoad = Math.min(solarGen, totalDemand);
        double surplusSolar = solarGen - solarToLoad;
        double netDeficit = totalDemand - solarToLoad;

        if (solarToLoad > 0.0) {
            logs.add(String.format("Solar PV supplied %.2f kW directly to local load", solarToLoad));
        }

        double solarToBattery = 0.0;
        double solarToGrid = 0.0;
        double curtailedSolar = 0.0;

        double batteryDischarge = 0.0;
        double gridToLoad = 0.0;
        double unmetCritical = 0.0;
        double unmetNonCritical = 0.0;

        if (surplusSolar > 0.0) {
            // Charge battery with surplus solar
            double maxChargeFeasible = Math.min(snapshot.batteryFeasibleChargeKw(), snapshot.inverterMaxChargeKw());
            solarToBattery = Math.min(surplusSolar, maxChargeFeasible);
            surplusSolar -= solarToBattery;

            if (solarToBattery > 0.0) {
                logs.add(String.format("Surplus solar charged battery with %.2f kW", solarToBattery));
            } else if (snapshot.batterySocPercent() >= snapshot.batteryMaxSocPercent()) {
                logs.add("Battery is at maximum SOC; cannot absorb surplus solar");
            }

            // Export remaining solar if grid is available and in grid-connected state
            if (surplusSolar > 0.0) {
                if (gridState.canExport() && snapshot.gridMaxExportKw() > 0.0) {
                    solarToGrid = Math.min(surplusSolar, snapshot.gridMaxExportKw());
                    surplusSolar -= solarToGrid;
                    logs.add(String.format("Exported %.2f kW excess solar to utility grid", solarToGrid));
                } else if (!gridState.canExport()) {
                    logs.add("Microgrid is Islanded; grid export prohibited");
                }

                if (surplusSolar > 0.0) {
                    curtailedSolar = surplusSolar;
                    logs.add(String.format("Curtailed %.2f kW surplus solar due to export/storage limits", curtailedSolar));
                }
            }
        } else if (netDeficit > 0.0) {
            // Deficit: discharge battery
            double maxDischargeFeasible = Math.min(snapshot.batteryFeasibleDischargeKw(), snapshot.inverterMaxDischargeKw());
            batteryDischarge = Math.min(netDeficit, maxDischargeFeasible);
            netDeficit -= batteryDischarge;

            if (batteryDischarge > 0.0) {
                logs.add(String.format("Battery discharged %.2f kW to supply local deficit", batteryDischarge));
            } else if (snapshot.batterySocPercent() <= snapshot.batteryMinSocPercent()) {
                logs.add("Battery is at minimum SOC; further discharge prevented");
            }

            // If deficit remains, import from grid if permissible
            if (netDeficit > 0.0) {
                if (gridState.canImport() && snapshot.gridMaxImportKw() > 0.0) {
                    gridToLoad = Math.min(netDeficit, snapshot.gridMaxImportKw());
                    netDeficit -= gridToLoad;
                    logs.add(String.format("Imported %.2f kW from utility grid", gridToLoad));
                } else if (!gridState.canImport()) {
                    logs.add("Microgrid is Islanded; grid import unavailable");
                }

                // If deficit still remains, allocate unmet demand prioritizing critical loads
                if (netDeficit > 0.0) {
                    double totalServed = solarToLoad + batteryDischarge + gridToLoad;
                    if (totalServed < criticalDemand) {
                        unmetCritical = criticalDemand - totalServed;
                        unmetNonCritical = nonCriticalDemand;
                        logs.add(String.format("CRITICAL LOAD SHORTAGE: %.2f kW critical demand unmet!", unmetCritical));
                    } else {
                        unmetCritical = 0.0;
                        unmetNonCritical = netDeficit;
                        logs.add(String.format("Non-critical demand partially shed: %.2f kW unmet", unmetNonCritical));
                    }
                }
            }
        } else {
            logs.add("Solar generation exactly matched total demand; power balance neutral");
        }

        return new PowerPlan(
                solarToLoad,
                solarToBattery,
                solarToGrid,
                batteryDischarge,
                gridToLoad,
                curtailedSolar,
                unmetCritical,
                unmetNonCritical,
                logs);
    }
}

