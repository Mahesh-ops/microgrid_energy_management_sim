package edu.bits.ooad.microgrid.domain.observer;

import edu.bits.ooad.microgrid.domain.model.SimulationResult;
import edu.bits.ooad.microgrid.domain.state.GridStateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObserverPatternTest {

    static class TestSubject implements SimulationSubject {
        private final List<SimulationObserver> observers = new ArrayList<>();

        @Override
        public void registerObserver(SimulationObserver observer) {
            observers.add(observer);
        }

        @Override
        public void removeObserver(SimulationObserver observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyStepCompleted(SimulationResult result) {
            for (SimulationObserver o : observers) {
                o.onSimulationStepCompleted(result);
            }
        }

        @Override
        public void notifyStateChanged(GridStateType previousState, GridStateType newState, String reason) {
            for (SimulationObserver o : observers) {
                o.onStateChanged(previousState, newState, reason);
            }
        }
    }

    static class TestObserver implements SimulationObserver {
        final List<SimulationResult> receivedResults = new ArrayList<>();
        final List<String> receivedTransitions = new ArrayList<>();

        @Override
        public void onSimulationStepCompleted(SimulationResult result) {
            receivedResults.add(result);
        }

        @Override
        public void onStateChanged(GridStateType previousState, GridStateType newState, String reason) {
            receivedTransitions.add(previousState + "->" + newState + ":" + reason);
        }
    }

    @Test
    @DisplayName("Observers should receive simulation results and state change events")
    void testObserverNotifications() {
        TestSubject subject = new TestSubject();
        TestObserver observer = new TestObserver();

        subject.registerObserver(observer);

        SimulationResult result = new SimulationResult(
                1, "GridConnected", "Self-Consumption",
                50.0, 50.0, 10.0,
                50.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0,
                50.0, 0.0, 0.0, 0.0,
                60.0, 60.0, List.of("Neutral step"));

        subject.notifyStepCompleted(result);
        assertEquals(1, observer.receivedResults.size());
        assertEquals(1, observer.receivedResults.get(0).stepIndex());

        subject.notifyStateChanged(
                GridStateType.GRID_CONNECTED,
                GridStateType.ISLANDED,
                "Grid loss detected");

        assertEquals(1, observer.receivedTransitions.size());
        assertTrue(observer.receivedTransitions.get(0).contains("Grid loss detected"));

        // Test unregistering
        subject.removeObserver(observer);
        subject.notifyStepCompleted(result);
        assertEquals(1, observer.receivedResults.size());
    }
}

