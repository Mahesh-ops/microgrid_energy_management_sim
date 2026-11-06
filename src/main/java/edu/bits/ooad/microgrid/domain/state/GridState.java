package edu.bits.ooad.microgrid.domain.state;

/**
 * State interface encapsulating mode-specific operating rules and transition logic.
 * Decouples the controller from state-specific conditional logic.
 */
public interface GridState {

    /**
     * Identifies the current state type.
     */
    GridStateType getStateType();

    /**
     * Human-readable state name.
     */
    String getStateName();

    /**
     * Whether importing energy from the utility grid is permissible in this state.
     */
    boolean canImport();

    /**
     * Whether exporting excess energy to the utility grid is permissible in this state.
     */
    boolean canExport();

    /**
     * Evaluates external physical grid status and initiates state transitions if needed.
     */
    void evaluateState(GridContext context, boolean physicalGridAvailable);

    /**
     * Attempts a simulated reconnection sequence to return to Grid-Connected operation.
     *
     * @return true if reconnection succeeded and state was transitioned; false otherwise.
     */
    boolean attemptReconnection(GridContext context);
}

