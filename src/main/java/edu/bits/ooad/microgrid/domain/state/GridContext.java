package edu.bits.ooad.microgrid.domain.state;

/**
 * Context interface for managing grid operating state transitions.
 * Implements the State Pattern context role.
 */
public interface GridContext {

    /**
     * Updates the active operating state.
     */
    void setGridState(GridState state);

    /**
     * Gets the current operating state.
     */
    GridState getGridState();

    /**
     * Checks if the physical utility grid is reported available.
     */
    boolean isGridPhysicallyAvailable();

    /**
     * Records a state change event.
     */
    void recordStateTransition(GridStateType from, GridStateType to, String reason);
}

