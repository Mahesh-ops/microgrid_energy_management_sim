package edu.bits.ooad.microgrid.domain.strategy;

/**
 * Enumeration of supported energy management strategies.
 */
public enum StrategyType {
    SELF_CONSUMPTION("Self-Consumption"),
    PEAK_SHAVING("Peak Shaving");

    private final String displayName;

    StrategyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static StrategyType fromString(String text) {
        if (text == null) {
            return SELF_CONSUMPTION;
        }
        for (StrategyType s : StrategyType.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return SELF_CONSUMPTION;
    }
}

