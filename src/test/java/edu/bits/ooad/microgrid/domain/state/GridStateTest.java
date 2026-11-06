package edu.bits.ooad.microgrid.domain.state;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GridStateTest {

    static class MockGridContext implements GridContext {
        GridState state = new GridConnectedState();
        boolean gridAvailable = true;
        final List<String> transitionLogs = new ArrayList<>();

        @Override
        public void setGridState(GridState state) {
            this.state = state;
        }

        @Override
        public GridState getGridState() {
            return state;
        }

        @Override
        public boolean isGridPhysicallyAvailable() {
            return gridAvailable;
        }

        @Override
        public void recordStateTransition(GridStateType from, GridStateType to, String reason) {
            transitionLogs.add(from + " -> " + to + ": " + reason);
        }
    }

    @Test
    @DisplayName("GridConnectedState should permit import and export")
    void testGridConnectedPermissions() {
        GridState state = new GridConnectedState();
        assertEquals(GridStateType.GRID_CONNECTED, state.getStateType());
        assertTrue(state.canImport());
        assertTrue(state.canExport());
    }

    @Test
    @DisplayName("IslandedState should prohibit both import and export")
    void testIslandedPermissions() {
        GridState state = new IslandedState();
        assertEquals(GridStateType.ISLANDED, state.getStateType());
        assertFalse(state.canImport());
        assertFalse(state.canExport());
    }

    @Test
    @DisplayName("GridConnectedState should transition to IslandedState when grid drops")
    void testGridFailureTransition() {
        MockGridContext context = new MockGridContext();
        context.gridAvailable = false;

        context.getGridState().evaluateState(context, false);
        assertEquals(GridStateType.ISLANDED, context.getGridState().getStateType());
        assertEquals(1, context.transitionLogs.size());
        assertTrue(context.transitionLogs.get(0).contains("transitioning to autonomous Islanded mode"));
    }

    @Test
    @DisplayName("IslandedState should not reconnect until reconnection verification succeeds")
    void testIslandedReconnectionCheck() {
        MockGridContext context = new MockGridContext();
        context.setGridState(new IslandedState());
        context.gridAvailable = false;

        // If physical grid is still down, reconnection fails
        boolean reconnected = context.getGridState().attemptReconnection(context);
        assertFalse(reconnected);
        assertEquals(GridStateType.ISLANDED, context.getGridState().getStateType());

        // When physical grid returns and attemptReconnection is called
        context.gridAvailable = true;
        boolean nowReconnected = context.getGridState().attemptReconnection(context);
        assertTrue(nowReconnected);
        assertEquals(GridStateType.GRID_CONNECTED, context.getGridState().getStateType());
    }
}

