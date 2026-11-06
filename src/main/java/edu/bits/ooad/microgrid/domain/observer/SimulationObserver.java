package edu.bits.ooad.microgrid.domain.observer;

import edu.bits.ooad.microgrid.domain.model.SimulationResult;
import edu.bits.ooad.microgrid.domain.state.GridStateType;

/**
 * Observer interface for entities interested in simulation events and state transitions.
 * Implements the Observer Pattern to decouple the simulation core from logging,
 * telemetry, and UI updates.
 */
public interface SimulationObserver {

    /**
     * Called whenever a simulation control step has successfully completed.
     */
    void onSimulationStepCompleted(SimulationResult result);

    /**
     * Called whenever the microgrid operating state transitions.
     */
    void onStateChanged(GridStateType previousState, GridStateType newState, String reason);
}

