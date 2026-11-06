package edu.bits.ooad.microgrid.domain.strategy;

import edu.bits.ooad.microgrid.domain.model.MeasurementSnapshot;
import edu.bits.ooad.microgrid.domain.model.PowerPlan;
import edu.bits.ooad.microgrid.domain.state.GridState;

/**
 * Strategy interface defining the contract for calculating microgrid power dispatch plans.
 * Decouples the controller coordination logic from specific energy optimization algorithms.
 */
public interface EnergyManagementStrategy {

    /**
     * Calculates a proposed PowerPlan based on current measurements and active grid state.
     *
     * @param snapshot Current immutable measurements and limits
     * @param gridState Current active grid operating state
     * @return Proposed power allocation plan
     */
    PowerPlan calculatePlan(MeasurementSnapshot snapshot, GridState gridState);

    /**
     * Gets the strategy classification type.
     */
    StrategyType getStrategyType();

    /**
     * Gets the human-readable strategy name.
     */
    String getStrategyName();
}

