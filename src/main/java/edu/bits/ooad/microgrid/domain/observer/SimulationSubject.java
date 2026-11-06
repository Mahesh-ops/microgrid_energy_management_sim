package edu.bits.ooad.microgrid.domain.observer;

import edu.bits.ooad.microgrid.domain.model.SimulationResult;
import edu.bits.ooad.microgrid.domain.state.GridStateType;

/**
 * Subject interface in the Observer Pattern.
 * Manages subscription and event dispatching to registered observers.
 */
public interface SimulationSubject {

    void registerObserver(SimulationObserver observer);

    void removeObserver(SimulationObserver observer);

    void notifyStepCompleted(SimulationResult result);

    void notifyStateChanged(GridStateType previousState, GridStateType newState, String reason);
}

