package edu.bits.ooad.microgrid.domain.state;

/**
 * Concrete state representing islanded (autonomous) microgrid operation.
 * The microgrid is electrically isolated from the main grid.
 * Grid import and export are strictly prohibited (0.0 kW).
 * Critical loads receive priority over non-critical loads during power shortages.
 */
public class IslandedState implements GridState {

    @Override
    public GridStateType getStateType() {
        return GridStateType.ISLANDED;
    }

    @Override
    public String getStateName() {
        return "Islanded";
    }

    @Override
    public boolean canImport() {
        return false;
    }

    @Override
    public boolean canExport() {
        return false;
    }

    @Override
    public void evaluateState(GridContext context, boolean physicalGridAvailable) {
        // In islanded mode, intentional reconnection requires safe verification
        // rather than accidental re-closure. If grid returns, system stays islanded
        // until attemptReconnection is invoked or auto-sync check is triggered.
    }

    @Override
    public boolean attemptReconnection(GridContext context) {
        if (!context.isGridPhysicallyAvailable()) {
            context.recordStateTransition(
                    GridStateType.ISLANDED,
                    GridStateType.ISLANDED,
                    "Reconnection check failed: physical utility grid is still offline");
            return false;
        }

        // Simulated voltage and frequency synchronization check passed
        context.recordStateTransition(
                GridStateType.ISLANDED,
                GridStateType.GRID_CONNECTED,
                "Simulated synchronization check passed; successfully reconnected to utility grid");
        context.setGridState(new GridConnectedState());
        return true;
    }

    @Override
    public String toString() {
        return getStateName();
    }
}

