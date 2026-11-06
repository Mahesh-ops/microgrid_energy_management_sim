package edu.bits.ooad.microgrid.application;

import edu.bits.ooad.microgrid.domain.factory.BatteryFactory;
import edu.bits.ooad.microgrid.domain.factory.BatteryType;
import edu.bits.ooad.microgrid.domain.model.*;
import edu.bits.ooad.microgrid.domain.observer.SimulationObserver;
import edu.bits.ooad.microgrid.domain.observer.SimulationSubject;
import edu.bits.ooad.microgrid.domain.service.PlanValidator;
import edu.bits.ooad.microgrid.domain.service.ValidationResult;
import edu.bits.ooad.microgrid.domain.state.GridConnectedState;
import edu.bits.ooad.microgrid.domain.state.GridContext;
import edu.bits.ooad.microgrid.domain.state.GridState;
import edu.bits.ooad.microgrid.domain.state.GridStateType;
import edu.bits.ooad.microgrid.domain.strategy.EnergyManagementStrategy;
import edu.bits.ooad.microgrid.domain.strategy.SelfConsumptionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Coordinates a microgrid simulation control cycle.
 * Operates on abstractions (EnergyManagementStrategy, GridState, Battery)
 * conforming to Single Responsibility, Open/Closed, and Dependency Inversion principles.
 */
public class MicrogridController implements GridContext, SimulationSubject {

    private SolarPV solarPV;
    private Load load;
    private Battery battery;
    private Inverter inverter;
    private UtilityGrid utilityGrid;

    private EnergyManagementStrategy strategy;
    private GridState gridState;
    private final PlanValidator validator;
    private final List<SimulationObserver> observers = new CopyOnWriteArrayList<>();

    private int stepIndex = 0;
    private Double peakShavingTargetKw = 25.0;

    public MicrogridController(
            SolarPV solarPV,
            Load load,
            Battery battery,
            Inverter inverter,
            UtilityGrid utilityGrid,
            EnergyManagementStrategy strategy,
            GridState gridState,
            PlanValidator validator) {

        this.solarPV = solarPV;
        this.load = load;
        this.battery = battery;
        this.inverter = inverter;
        this.utilityGrid = utilityGrid;
        this.strategy = (strategy != null) ? strategy : new SelfConsumptionStrategy();
        this.gridState = (gridState != null) ? gridState : new GridConnectedState();
        this.validator = (validator != null) ? validator : new PlanValidator();
    }

    /**
     * Default constructor with sensible educational defaults based on case study.
     */
    public static MicrogridController createDefault() {
        SolarPV solar = new SolarPV(100.0, 45.0);
        Load load = new Load(60.0, 20.0);
        BatteryFactory factory = new BatteryFactory();
        Battery battery = factory.createBattery(BatteryType.LITHIUM_ION, 100.0, 50.0);
        Inverter inverter = new Inverter(60.0);
        UtilityGrid grid = new UtilityGrid(true, 100.0, 50.0);

        return new MicrogridController(
                solar, load, battery, inverter, grid,
                new SelfConsumptionStrategy(),
                new GridConnectedState(),
                new PlanValidator());
    }

    /**
     * Executes one complete supervisory control cycle.
     *
     * @param stepHours Duration of control cycle in hours (default 1.0)
     * @return Result record of applied power dispatch and component outcomes
     */
    public synchronized SimulationResult executeStep(double stepHours) {
        if (stepHours <= 0.0) {
            stepHours = 1.0;
        }

        // 1. Check and reconcile grid state against physical availability
        gridState.evaluateState(this, utilityGrid.isAvailable());

        // 2. Gather immutable measurement snapshot
        double feasibleCharge = battery.calculateMaxChargePower(stepHours);
        double feasibleDischarge = battery.calculateMaxDischargePower(stepHours);

        MeasurementSnapshot snapshot = new MeasurementSnapshot(
                solarPV.getAvailablePowerKw(),
                load.getTotalDemandKw(),
                load.getCriticalDemandKw(),
                battery.getSocPercent(),
                battery.getMinSocPercent(),
                battery.getMaxSocPercent(),
                feasibleCharge,
                feasibleDischarge,
                inverter.getMaxChargeLimitKw(),
                inverter.getMaxDischargeLimitKw(),
                utilityGrid.isAvailable(),
                utilityGrid.getMaxImportKw(),
                utilityGrid.getMaxExportKw(),
                stepHours,
                peakShavingTargetKw);

        // 3. Energy strategy proposes power plan
        PowerPlan proposedPlan = strategy.calculatePlan(snapshot, gridState);

        // 4. Validate and safeguard plan
        ValidationResult valResult = validator.validate(proposedPlan, snapshot, gridState);
        PowerPlan plan = valResult.validatedPlan();

        // 5. Apply validated plan to physical components
        double initialSoc = battery.getSocPercent();

        if (plan.getBatteryChargeKw() > 0.0) {
            battery.charge(plan.getBatteryChargeKw(), stepHours);
        } else if (plan.getBatteryDischargeKw() > 0.0) {
            battery.discharge(plan.getBatteryDischargeKw(), stepHours);
        }

        double finalSoc = battery.getSocPercent();
        stepIndex++;

        // 6. Assemble simulation result
        SimulationResult result = new SimulationResult(
                stepIndex,
                gridState.getStateName(),
                strategy.getStrategyName(),
                snapshot.solarGenerationKw(),
                snapshot.totalDemandKw(),
                snapshot.criticalDemandKw(),
                plan.getSolarToLoadKw(),
                plan.getSolarToBatteryKw(),
                plan.getSolarToGridKw(),
                plan.getBatteryDischargeKw(),
                plan.getBatteryChargeKw(),
                plan.getGridImportKw(),
                plan.getGridExportKw(),
                plan.getTotalSuppliedLoadKw(),
                plan.getTotalUnmetLoadKw(),
                plan.getUnmetCriticalLoadKw(),
                plan.getCurtailedSolarKw(),
                initialSoc,
                finalSoc,
                valResult.auditLogs(),
                stepHours);

        // 7. Notify registered observers
        notifyStepCompleted(result);

        return result;
    }

    public synchronized SimulationResult executeStep() {
        return executeStep(1.0);
    }

    /**
     * Simulates external grid outage / trip at Point of Common Coupling.
     */
    public synchronized void simulateGridFailure() {
        utilityGrid.setAvailable(false);
        gridState.evaluateState(this, false);
    }

    /**
     * Simulates restoration and synchronization attempt back to main grid.
     */
    public synchronized boolean simulateGridRestore() {
        utilityGrid.setAvailable(true);
        return gridState.attemptReconnection(this);
    }

    // GridContext implementation
    @Override
    public synchronized void setGridState(GridState state) {
        this.gridState = state;
    }

    @Override
    public synchronized GridState getGridState() {
        return gridState;
    }

    @Override
    public synchronized boolean isGridPhysicallyAvailable() {
        return utilityGrid.isAvailable();
    }

    @Override
    public synchronized void recordStateTransition(GridStateType from, GridStateType to, String reason) {
        notifyStateChanged(from, to, reason);
    }

    // SimulationSubject implementation
    @Override
    public void registerObserver(SimulationObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(SimulationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyStepCompleted(SimulationResult result) {
        for (SimulationObserver observer : observers) {
            observer.onSimulationStepCompleted(result);
        }
    }

    @Override
    public void notifyStateChanged(GridStateType previousState, GridStateType newState, String reason) {
        for (SimulationObserver observer : observers) {
            observer.onStateChanged(previousState, newState, reason);
        }
    }

    // Getters and Mutators
    public synchronized SolarPV getSolarPV() {
        return solarPV;
    }

    public synchronized Load getLoad() {
        return load;
    }

    public synchronized Battery getBattery() {
        return battery;
    }

    public synchronized Inverter getInverter() {
        return inverter;
    }

    public synchronized UtilityGrid getUtilityGrid() {
        return utilityGrid;
    }

    public synchronized EnergyManagementStrategy getStrategy() {
        return strategy;
    }

    public synchronized void setStrategy(EnergyManagementStrategy strategy) {
        if (strategy != null) {
            this.strategy = strategy;
        }
    }

    public synchronized void setBattery(Battery battery) {
        if (battery != null) {
            this.battery = battery;
        }
    }

    public synchronized void updateInputs(double solarKw, double totalDemandKw, double criticalDemandKw) {
        solarPV.setGeneration(solarKw);
        load.update(totalDemandKw, criticalDemandKw);
    }

    public synchronized int getStepIndex() {
        return stepIndex;
    }

    public synchronized Double getPeakShavingTargetKw() {
        return peakShavingTargetKw;
    }

    public synchronized void setPeakShavingTargetKw(Double peakShavingTargetKw) {
        this.peakShavingTargetKw = peakShavingTargetKw;
    }

    public synchronized void reset(double initialSoc) {
        stepIndex = 0;
        battery.setSocPercent(initialSoc);
        utilityGrid.setAvailable(true);
        gridState = new GridConnectedState();
    }
}

