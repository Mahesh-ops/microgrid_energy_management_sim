package edu.bits.ooad.microgrid.domain.strategy;

import edu.bits.ooad.microgrid.domain.model.MeasurementSnapshot;
import edu.bits.ooad.microgrid.domain.model.PowerPlan;
import edu.bits.ooad.microgrid.domain.state.GridState;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete strategy implementing Peak Shaving.
 * Attempts to keep grid import at or below a target threshold (peakShavingTargetKw)
 * by deploying battery energy storage during high demand periods.
 */
public class PeakShavingStrategy implements EnergyManagementStrategy {

    private final double defaultTargetImportKw;

    public PeakShavingStrategy(double defaultTargetImportKw) {
        this.defaultTargetImportKw = Math.max(0.0, defaultTargetImportKw);
    }

    public PeakShavingStrategy() {
        this(20.0); // 20 kW default peak shaving limit
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.PEAK_SHAVING;
    }

    @Override
    public String getStrategyName() {
        return "Peak Shaving";
    }

    @Override
    public PowerPlan calculatePlan(MeasurementSnapshot snapshot, GridState gridState) {
        List<String> logs = new ArrayList<>();

        double targetImport = (snapshot.peakShavingTargetKw() != null)
                ? snapshot.peakShavingTargetKw()
                : defaultTargetImportKw;

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
            // In surplus, charge battery to prepare for upcoming peaks
            double maxChargeFeasible = Math.min(snapshot.batteryFeasibleChargeKw(), snapshot.inverterMaxChargeKw());
            solarToBattery = Math.min(surplusSolar, maxChargeFeasible);
            surplusSolar -= solarToBattery;

            if (solarToBattery > 0.0) {
                logs.add(String.format("Surplus solar charged battery with %.2f kW for future peak shaving", solarToBattery));
            }

            if (surplusSolar > 0.0) {
                if (gridState.canExport() && snapshot.gridMaxExportKw() > 0.0) {
                    solarToGrid = Math.min(surplusSolar, snapshot.gridMaxExportKw());
                    surplusSolar -= solarToGrid;
                    logs.add(String.format("Exported %.2f kW surplus solar to grid", solarToGrid));
                }
                if (surplusSolar > 0.0) {
                    curtailedSolar = surplusSolar;
                    logs.add(String.format("Curtailed %.2f kW solar", curtailedSolar));
                }
            }
        } else if (netDeficit > 0.0) {
            if (!gridState.canImport()) {
                // Islanded: grid unavailable, all deficit falls onto battery
                double maxDischargeFeasible = Math.min(snapshot.batteryFeasibleDischargeKw(), snapshot.inverterMaxDischargeKw());
                batteryDischarge = Math.min(netDeficit, maxDischargeFeasible);
                netDeficit -= batteryDischarge;
                logs.add(String.format("Islanded mode: Battery discharged %.2f kW", batteryDischarge));
            } else {
                // Grid-connected: evaluate against peak shaving target
                if (netDeficit <= targetImport) {
                    // Demand is below target; rely on grid and preserve battery storage
                    gridToLoad = Math.min(netDeficit, snapshot.gridMaxImportKw());
                    netDeficit -= gridToLoad;
                    logs.add(String.format("Grid import (%.2f kW) is below peak target (%.2f kW); battery preserved",
                            gridToLoad, targetImport));
                } else {
                    // Demand exceeds peak shaving target -> discharge battery to shave the excess peak
                    double peakExcess = netDeficit - targetImport;
                    double maxDischargeFeasible = Math.min(snapshot.batteryFeasibleDischargeKw(), snapshot.inverterMaxDischargeKw());
                    batteryDischarge = Math.min(peakExcess, maxDischargeFeasible);
                    netDeficit -= batteryDischarge;

                    logs.add(String.format("Peak shaving active: discharged battery %.2f kW to reduce grid spike", batteryDischarge));

                    // Now import up to the peak target
                    double permissibleImport = Math.min(targetImport, snapshot.gridMaxImportKw());
                    double normalImport = Math.min(netDeficit, permissibleImport);
                    gridToLoad += normalImport;
                    netDeficit -= normalImport;

                    // If still deficit (because battery couldn't shave fully), grid can absorb up to physical feeder limit
                    if (netDeficit > 0.0 && gridToLoad < snapshot.gridMaxImportKw()) {
                        double additionalImport = Math.min(netDeficit, snapshot.gridMaxImportKw() - gridToLoad);
                        gridToLoad += additionalImport;
                        netDeficit -= additionalImport;
                        logs.add(String.format("Grid import reached %.2f kW (exceeded peak target %.2f kW due to battery limits)",
                                gridToLoad, targetImport));
                    }
                }
            }

            // Unmet load check
            if (netDeficit > 0.0) {
                double totalServed = solarToLoad + batteryDischarge + gridToLoad;
                if (totalServed < criticalDemand) {
                    unmetCritical = criticalDemand - totalServed;
                    unmetNonCritical = nonCriticalDemand;
                    logs.add(String.format("CRITICAL SHORTAGE: %.2f kW unmet critical demand", unmetCritical));
                } else {
                    unmetCritical = 0.0;
                    unmetNonCritical = netDeficit;
                    logs.add(String.format("Load partially shed: %.2f kW unmet non-critical demand", unmetNonCritical));
                }
            }
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

