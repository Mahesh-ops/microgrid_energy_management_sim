package edu.bits.ooad.microgrid.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Proposed power allocation plan produced by an EnergyManagementStrategy.
 * Carries proposed allocations; does not mutate components directly.
 */
public class PowerPlan {

    private final double solarToLoadKw;
    private final double solarToBatteryKw;
    private final double solarToGridKw;
    private final double batteryDischargeToLoadKw;
    private final double gridToLoadKw;
    private final double curtailedSolarKw;
    private final double unmetCriticalLoadKw;
    private final double unmetNonCriticalLoadKw;
    private final List<String> decisionLogs;

    public PowerPlan(
            double solarToLoadKw,
            double solarToBatteryKw,
            double solarToGridKw,
            double batteryDischargeToLoadKw,
            double gridToLoadKw,
            double curtailedSolarKw,
            double unmetCriticalLoadKw,
            double unmetNonCriticalLoadKw,
            List<String> decisionLogs) {

        this.solarToLoadKw = Math.max(0.0, solarToLoadKw);
        this.solarToBatteryKw = Math.max(0.0, solarToBatteryKw);
        this.solarToGridKw = Math.max(0.0, solarToGridKw);
        this.batteryDischargeToLoadKw = Math.max(0.0, batteryDischargeToLoadKw);
        this.gridToLoadKw = Math.max(0.0, gridToLoadKw);
        this.curtailedSolarKw = Math.max(0.0, curtailedSolarKw);
        this.unmetCriticalLoadKw = Math.max(0.0, unmetCriticalLoadKw);
        this.unmetNonCriticalLoadKw = Math.max(0.0, unmetNonCriticalLoadKw);
        this.decisionLogs = (decisionLogs != null) ? new ArrayList<>(decisionLogs) : new ArrayList<>();
    }

    public double getSolarToLoadKw() {
        return solarToLoadKw;
    }

    public double getSolarToBatteryKw() {
        return solarToBatteryKw;
    }

    public double getSolarToGridKw() {
        return solarToGridKw;
    }

    public double getBatteryDischargeToLoadKw() {
        return batteryDischargeToLoadKw;
    }

    public double getGridToLoadKw() {
        return gridToLoadKw;
    }

    public double getCurtailedSolarKw() {
        return curtailedSolarKw;
    }

    public double getUnmetCriticalLoadKw() {
        return unmetCriticalLoadKw;
    }

    public double getUnmetNonCriticalLoadKw() {
        return unmetNonCriticalLoadKw;
    }

    public List<String> getDecisionLogs() {
        return Collections.unmodifiableList(decisionLogs);
    }

    public double getTotalSuppliedLoadKw() {
        return solarToLoadKw + batteryDischargeToLoadKw + gridToLoadKw;
    }

    public double getTotalUnmetLoadKw() {
        return unmetCriticalLoadKw + unmetNonCriticalLoadKw;
    }

    public double getBatteryChargeKw() {
        return solarToBatteryKw;
    }

    public double getBatteryDischargeKw() {
        return batteryDischargeToLoadKw;
    }

    public double getGridImportKw() {
        return gridToLoadKw;
    }

    public double getGridExportKw() {
        return solarToGridKw;
    }

    @Override
    public String toString() {
        return "PowerPlan{"
                + "solarToLoad=" + String.format("%.2f", solarToLoadKw)
                + ", solarToBattery=" + String.format("%.2f", solarToBatteryKw)
                + ", solarToGrid=" + String.format("%.2f", solarToGridKw)
                + ", batteryDischarge=" + String.format("%.2f", batteryDischargeToLoadKw)
                + ", gridToLoad=" + String.format("%.2f", gridToLoadKw)
                + ", unmetCritical=" + String.format("%.2f", unmetCriticalLoadKw)
                + ", unmetNonCritical=" + String.format("%.2f", unmetNonCriticalLoadKw)
                + "}";
    }
}

