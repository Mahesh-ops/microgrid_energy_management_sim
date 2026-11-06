package edu.bits.ooad.microgrid.domain.model;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the execution result of a single simulated control step.
 * Used for performance monitoring, UI rendering, and CSV exports.
 */
public record SimulationResult(
        int stepIndex,
        String operatingState,
        String strategyName,
        double solarGenerationKw,
        double totalDemandKw,
        double criticalDemandKw,
        double solarToLoadKw,
        double solarToBatteryKw,
        double solarToGridKw,
        double batteryDischargeKw,
        double batteryChargeKw,
        double gridImportKw,
        double gridExportKw,
        double servedLoadKw,
        double unmetLoadKw,
        double unmetCriticalLoadKw,
        double curtailedSolarKw,
        double initialBatterySocPercent,
        double finalBatterySocPercent,
        List<String> events,
        double stepHours) {

    public SimulationResult {
        events = (events != null) ? List.copyOf(events) : Collections.emptyList();
        if (stepHours <= 0.0) {
            stepHours = 1.0;
        }
    }

    /**
     * Backward-compatible 20-parameter constructor defaulting stepHours to 1.0.
     */
    public SimulationResult(
            int stepIndex,
            String operatingState,
            String strategyName,
            double solarGenerationKw,
            double totalDemandKw,
            double criticalDemandKw,
            double solarToLoadKw,
            double solarToBatteryKw,
            double solarToGridKw,
            double batteryDischargeKw,
            double batteryChargeKw,
            double gridImportKw,
            double gridExportKw,
            double servedLoadKw,
            double unmetLoadKw,
            double unmetCriticalLoadKw,
            double curtailedSolarKw,
            double initialBatterySocPercent,
            double finalBatterySocPercent,
            List<String> events) {
        this(stepIndex, operatingState, strategyName, solarGenerationKw, totalDemandKw, criticalDemandKw,
                solarToLoadKw, solarToBatteryKw, solarToGridKw, batteryDischargeKw, batteryChargeKw,
                gridImportKw, gridExportKw, servedLoadKw, unmetLoadKw, unmetCriticalLoadKw,
                curtailedSolarKw, initialBatterySocPercent, finalBatterySocPercent, events, 1.0);
    }

    /**
     * Formats result as a CSV row matching standard header.
     */
    public String toCsvRow() {
        return String.format(
                "%d,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,\"%s\"",
                stepIndex,
                operatingState,
                strategyName,
                solarGenerationKw,
                totalDemandKw,
                criticalDemandKw,
                solarToLoadKw,
                solarToBatteryKw,
                solarToGridKw,
                batteryDischargeKw,
                batteryChargeKw,
                gridImportKw,
                gridExportKw,
                servedLoadKw,
                unmetLoadKw,
                unmetCriticalLoadKw,
                curtailedSolarKw,
                initialBatterySocPercent,
                finalBatterySocPercent,
                stepHours,
                String.join("; ", events).replace("\"", "'"));
    }

    public static String getCsvHeader() {
        return "Step,State,Strategy,SolarGen_kW,TotalLoad_kW,CriticalLoad_kW,SolarToLoad_kW,"
                + "SolarToBattery_kW,SolarToGrid_kW,BatteryDischarge_kW,BatteryCharge_kW,GridImport_kW,"
                + "GridExport_kW,ServedLoad_kW,UnmetLoad_kW,UnmetCriticalLoad_kW,CurtailedSolar_kW,"
                + "InitialSOC_pct,FinalSOC_pct,StepDuration_h,Events";
    }
}

