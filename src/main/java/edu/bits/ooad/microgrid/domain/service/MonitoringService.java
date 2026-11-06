package edu.bits.ooad.microgrid.domain.service;

import edu.bits.ooad.microgrid.domain.model.SimulationResult;
import edu.bits.ooad.microgrid.domain.observer.SimulationObserver;
import edu.bits.ooad.microgrid.domain.state.GridStateType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Monitoring and telemetry service.
 * Implements SimulationObserver to capture step results and state changes without
 * tight coupling to simulation dispatch or UI presentation.
 */
public class MonitoringService implements SimulationObserver {

    private final List<SimulationResult> history = new ArrayList<>();
    private final List<String> transitionLogs = new ArrayList<>();

    private double totalSolarGeneratedKwh = 0.0;
    private double totalSolarConsumedKwh = 0.0;
    private double totalSolarExportedKwh = 0.0;
    private double totalGridImportedKwh = 0.0;
    private double totalLoadDemandedKwh = 0.0;
    private double totalLoadServedKwh = 0.0;
    private double totalUnmetLoadKwh = 0.0;
    private double totalUnmetCriticalLoadKwh = 0.0;
    private double totalSimulatedHours = 0.0;
    private double minSocObserved = 100.0;
    private double maxSocObserved = 0.0;

    @Override
    public synchronized void onSimulationStepCompleted(SimulationResult result) {
        history.add(result);

        double hours = (result.stepHours() > 0.0) ? result.stepHours() : 1.0;
        totalSimulatedHours += hours;
        totalSolarGeneratedKwh += result.solarGenerationKw() * hours;
        totalSolarConsumedKwh += (result.solarToLoadKw() + result.solarToBatteryKw()) * hours;
        totalSolarExportedKwh += result.solarToGridKw() * hours;
        totalGridImportedKwh += result.gridImportKw() * hours;
        totalLoadDemandedKwh += result.totalDemandKw() * hours;
        totalLoadServedKwh += result.servedLoadKw() * hours;
        totalUnmetLoadKwh += result.unmetLoadKw() * hours;
        totalUnmetCriticalLoadKwh += result.unmetCriticalLoadKw() * hours;

        minSocObserved = Math.min(minSocObserved, result.finalBatterySocPercent());
        maxSocObserved = Math.max(maxSocObserved, result.finalBatterySocPercent());
    }

    @Override
    public synchronized void onStateChanged(GridStateType previousState, GridStateType newState, String reason) {
        transitionLogs.add(String.format("[%s -> %s]: %s",
                previousState.getDisplayName(), newState.getDisplayName(), reason));
    }

    public synchronized List<SimulationResult> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    public synchronized List<String> getTransitionLogs() {
        return Collections.unmodifiableList(new ArrayList<>(transitionLogs));
    }

    public synchronized SimulationResult getLatestResult() {
        return history.isEmpty() ? null : history.get(history.size() - 1);
    }

    public synchronized int getStepCount() {
        return history.size();
    }

    public synchronized void clear() {
        history.clear();
        transitionLogs.clear();
        totalSimulatedHours = 0.0;
        totalSolarGeneratedKwh = 0.0;
        totalSolarConsumedKwh = 0.0;
        totalSolarExportedKwh = 0.0;
        totalGridImportedKwh = 0.0;
        totalLoadDemandedKwh = 0.0;
        totalLoadServedKwh = 0.0;
        totalUnmetLoadKwh = 0.0;
        totalUnmetCriticalLoadKwh = 0.0;
        minSocObserved = 100.0;
        maxSocObserved = 0.0;
    }

    public synchronized double getTotalSimulatedHours() {
        return totalSimulatedHours;
    }

    /**
     * Formats entire simulation history into RFC-compliant CSV format.
     */
    public synchronized String exportCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(SimulationResult.getCsvHeader()).append("\n");
        for (SimulationResult r : history) {
            sb.append(r.toCsvRow()).append("\n");
        }
        return sb.toString();
    }

    public synchronized double getTotalSolarGeneratedKwh() {
        return totalSolarGeneratedKwh;
    }

    public synchronized double getTotalSolarConsumedKwh() {
        return totalSolarConsumedKwh;
    }

    public synchronized double getTotalSolarExportedKwh() {
        return totalSolarExportedKwh;
    }

    public synchronized double getTotalGridImportedKwh() {
        return totalGridImportedKwh;
    }

    public synchronized double getTotalLoadDemandedKwh() {
        return totalLoadDemandedKwh;
    }

    public synchronized double getTotalLoadServedKwh() {
        return totalLoadServedKwh;
    }

    public synchronized double getTotalUnmetLoadKwh() {
        return totalUnmetLoadKwh;
    }

    public synchronized double getTotalUnmetCriticalLoadKwh() {
        return totalUnmetCriticalLoadKwh;
    }

    public synchronized double getMinSocObserved() {
        return history.isEmpty() ? 0.0 : minSocObserved;
    }

    public synchronized double getMaxSocObserved() {
        return history.isEmpty() ? 0.0 : maxSocObserved;
    }
}

