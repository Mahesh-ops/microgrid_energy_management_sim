package edu.bits.ooad.microgrid.application;

import edu.bits.ooad.microgrid.domain.model.SimulationResult;
import edu.bits.ooad.microgrid.domain.service.MonitoringService;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level facade orchestrating execution of single steps, step batches,
 * and comprehensive multi-hour scenario profiles.
 */
public class SimulationEngine {

    private final MicrogridController controller;
    private final MonitoringService monitoringService;

    public SimulationEngine(MicrogridController controller, MonitoringService monitoringService) {
        this.controller = controller;
        this.monitoringService = monitoringService;
        this.controller.registerObserver(this.monitoringService);
    }

    public static SimulationEngine createDefault() {
        MicrogridController controller = MicrogridController.createDefault();
        MonitoringService monitoring = new MonitoringService();
        return new SimulationEngine(controller, monitoring);
    }

    /**
     * Executes a single control step with default duration of 1.0 hour.
     */
    public SimulationResult runStep() {
        return runStep(1.0);
    }

    /**
     * Executes a single control step with user-specified duration in hours (e.g. 0.5 for 30 minutes).
     */
    public SimulationResult runStep(double stepHours) {
        return controller.executeStep(Math.max(0.01, stepHours));
    }

    /**
     * Executes multiple consecutive steps under current input parameters with default 1.0 hour duration.
     */
    public List<SimulationResult> runSteps(int count) {
        return runSteps(count, 1.0);
    }

    /**
     * Executes multiple consecutive steps under current input parameters with user-specified duration.
     */
    public List<SimulationResult> runSteps(int count, double stepHours) {
        List<SimulationResult> results = new ArrayList<>();
        double safeStepHours = Math.max(0.01, stepHours);
        for (int i = 0; i < count; i++) {
            results.add(controller.executeStep(safeStepHours));
        }
        return results;
    }

    /**
     * Runs an entire pre-configured scenario profile.
     */
    public List<SimulationResult> runScenario(Scenario scenario) {
        List<SimulationResult> results = new ArrayList<>();
        for (ScenarioStep step : scenario.getSteps()) {
            controller.updateInputs(step.solarGenerationKw(), step.totalDemandKw(), step.criticalDemandKw());
            if (step.gridAvailable()) {
                if (!controller.isGridPhysicallyAvailable()) {
                    controller.simulateGridRestore();
                }
            } else {
                if (controller.isGridPhysicallyAvailable()) {
                    controller.simulateGridFailure();
                }
            }
            results.add(controller.executeStep(1.0));
        }
        return results;
    }

    public void reset(double initialSoc) {
        monitoringService.clear();
        controller.reset(initialSoc);
    }

    public MicrogridController getController() {
        return controller;
    }

    public MonitoringService getMonitoringService() {
        return monitoringService;
    }
}

