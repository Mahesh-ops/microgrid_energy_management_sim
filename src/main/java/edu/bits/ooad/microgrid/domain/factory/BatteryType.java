package edu.bits.ooad.microgrid.domain.factory;

/**
 * Enumeration of supported battery technologies in the microgrid simulation.
 */
public enum BatteryType {
    LITHIUM_ION("Lithium-Ion (LiFePO4)"),
    LEAD_ACID("Lead-Acid (VRLA)"),
    SODIUM_NICKEL("Sodium-Nickel (ZEBRA)");

    private final String displayName;

    BatteryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BatteryType fromString(String text) {
        if (text == null) {
            return LITHIUM_ION;
        }
        for (BatteryType b : BatteryType.values()) {
            if (b.name().equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return LITHIUM_ION;
    }
}

