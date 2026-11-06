package edu.bits.ooad.microgrid.domain.state;

/**
 * Concrete state representing grid-connected microgrid operation.
 * Energy can be freely imported from or exported to the utility grid
 * within configured feeder ratings.
 */
public class GridConnectedState implements GridState {

    @Override
    public GridStateType getStateType() {
        return GridStateType.GRID_CONNECTED;
    }

    @Override
    public String getStateName() {
        return "Grid-Connected";
    }

    @Override
    public boolean canImport() {
        return true;
    }

    @Override
    public boolean canExport() {
        return true;
    }

    @Override
    public void evaluateState(GridContext context, boolean physicalGridAvailable) {
        if (!physicalGridAvailable) {
            context.recordStateTransition(
                    GridStateType.GRID_CONNECTED,
                    GridStateType.ISLANDED,
                    "Utility grid became unavailable; transitioning to autonomous Islanded mode");
            context.setGridState(new IslandedState());
        }
    }

    @Override
    public boolean attemptReconnection(GridContext context) {
        // Already connected
        return true;
    }

    @Override
    public String toString() {
        return getStateName();
    }
}

