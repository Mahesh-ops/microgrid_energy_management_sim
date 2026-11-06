package edu.bits.ooad.microgrid.domain.state;

/**
 * Operating states for microgrid grid-coupling mode.
 */
public enum GridStateType {
    GRID_CONNECTED("Grid-Connected"),
    ISLANDED("Islanded (Autonomous)");

    private final String displayName;

    GridStateType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

