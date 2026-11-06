package edu.bits.ooad.microgrid.domain.model;

/**
 * Abstract base class representing an electrical energy storage bank.
 * Encapsulates battery chemistry limits, state of charge (SOC) tracking,
 * and safe charge/discharge transitions.
 */
public abstract class Battery {

    private final String name;
    private final double capacityKwh;
    private double socPercent;
    private final double minSocPercent;
    private final double maxSocPercent;
    private final double maxChargePowerKw;
    private final double maxDischargePowerKw;
    private final double chargeEfficiency;
    private final double dischargeEfficiency;

    public Battery(
            String name,
            double capacityKwh,
            double socPercent,
            double minSocPercent,
            double maxSocPercent,
            double maxChargePowerKw,
            double maxDischargePowerKw,
            double chargeEfficiency,
            double dischargeEfficiency) {

        if (capacityKwh <= 0.0) {
            throw new IllegalArgumentException("Battery capacity must be > 0 kWh, got " + capacityKwh);
        }
        if (!(0.0 <= minSocPercent && minSocPercent < maxSocPercent && maxSocPercent <= 100.0)) {
            throw new IllegalArgumentException(
                    "Invalid SOC limits: min=" + minSocPercent + "%, max=" + maxSocPercent + "%. Must satisfy 0 <= min < max <= 100.");
        }
        if (socPercent < 0.0 || socPercent > 100.0) {
            throw new IllegalArgumentException("SOC percent must be between 0 and 100, got " + socPercent);
        }
        if (maxChargePowerKw < 0.0) {
            throw new IllegalArgumentException("Max charge power must be non-negative, got " + maxChargePowerKw);
        }
        if (maxDischargePowerKw < 0.0) {
            throw new IllegalArgumentException("Max discharge power must be non-negative, got " + maxDischargePowerKw);
        }
        if (chargeEfficiency <= 0.0 || chargeEfficiency > 1.0) {
            throw new IllegalArgumentException("Charge efficiency must be in (0, 1.0], got " + chargeEfficiency);
        }
        if (dischargeEfficiency <= 0.0 || dischargeEfficiency > 1.0) {
            throw new IllegalArgumentException("Discharge efficiency must be in (0, 1.0], got " + dischargeEfficiency);
        }

        this.name = name;
        this.capacityKwh = capacityKwh;
        this.socPercent = Math.min(maxSocPercent, Math.max(minSocPercent, socPercent));
        this.minSocPercent = minSocPercent;
        this.maxSocPercent = maxSocPercent;
        this.maxChargePowerKw = maxChargePowerKw;
        this.maxDischargePowerKw = maxDischargePowerKw;
        this.chargeEfficiency = chargeEfficiency;
        this.dischargeEfficiency = dischargeEfficiency;
    }

    public abstract String getTechnologyName();

    public double calculateMaxChargePower(double stepHours) {
        if (stepHours <= 0.0) {
            throw new IllegalArgumentException("Step duration must be positive, got " + stepHours);
        }
        if (socPercent >= maxSocPercent) {
            return 0.0;
        }
        double socRoom = Math.max(0.0, maxSocPercent - socPercent);
        double energyRoomKwh = (socRoom / 100.0) * capacityKwh;
        double maxPowerBySoc = energyRoomKwh / (stepHours * chargeEfficiency);
        return Math.min(maxChargePowerKw, maxPowerBySoc);
    }

    public double calculateMaxDischargePower(double stepHours) {
        if (stepHours <= 0.0) {
            throw new IllegalArgumentException("Step duration must be positive, got " + stepHours);
        }
        if (socPercent <= minSocPercent) {
            return 0.0;
        }
        double socAvailable = Math.max(0.0, socPercent - minSocPercent);
        double extractableEnergyKwh = (socAvailable / 100.0) * capacityKwh;
        double maxPowerBySoc = (extractableEnergyKwh * dischargeEfficiency) / stepHours;
        return Math.min(maxDischargePowerKw, maxPowerBySoc);
    }

    /**
     * Charges the battery by requested power in kW over stepHours.
     * Safely clamps power to allowable bounds.
     *
     * @return actual power charged in kW
     */
    public double charge(double powerKw, double stepHours) {
        if (powerKw < 0.0) {
            throw new IllegalArgumentException("Charge power must be non-negative, got " + powerKw);
        }
        if (stepHours <= 0.0) {
            throw new IllegalArgumentException("Step duration must be positive, got " + stepHours);
        }
        if (powerKw == 0.0 || socPercent >= maxSocPercent) {
            return 0.0;
        }

        double maxAllowed = calculateMaxChargePower(stepHours);
        double actualPower = Math.min(powerKw, maxAllowed);
        double deltaSoc = (actualPower * stepHours * chargeEfficiency / capacityKwh) * 100.0;
        this.socPercent = Math.min(maxSocPercent, this.socPercent + deltaSoc);
        return actualPower;
    }

    /**
     * Discharges the battery by requested power in kW over stepHours.
     * Safely clamps power to allowable bounds.
     *
     * @return actual power discharged in kW
     */
    public double discharge(double powerKw, double stepHours) {
        if (powerKw < 0.0) {
            throw new IllegalArgumentException("Discharge power must be non-negative, got " + powerKw);
        }
        if (stepHours <= 0.0) {
            throw new IllegalArgumentException("Step duration must be positive, got " + stepHours);
        }
        if (powerKw == 0.0 || socPercent <= minSocPercent) {
            return 0.0;
        }

        double maxAllowed = calculateMaxDischargePower(stepHours);
        double actualPower = Math.min(powerKw, maxAllowed);
        double deltaSoc = (actualPower * stepHours / (capacityKwh * dischargeEfficiency)) * 100.0;
        this.socPercent = Math.max(minSocPercent, this.socPercent - deltaSoc);
        return actualPower;
    }

    public String getName() {
        return name;
    }

    public double getCapacityKwh() {
        return capacityKwh;
    }

    public double getSocPercent() {
        return socPercent;
    }

    public void setSocPercent(double socPercent) {
        if (socPercent < 0.0 || socPercent > 100.0) {
            throw new IllegalArgumentException("SOC percent must be between 0 and 100, got " + socPercent);
        }
        this.socPercent = Math.min(maxSocPercent, Math.max(minSocPercent, socPercent));
    }

    public double getMinSocPercent() {
        return minSocPercent;
    }

    public double getMaxSocPercent() {
        return maxSocPercent;
    }

    public double getMaxChargePowerKw() {
        return maxChargePowerKw;
    }

    public double getMaxDischargePowerKw() {
        return maxDischargePowerKw;
    }

    public double getChargeEfficiency() {
        return chargeEfficiency;
    }

    public double getDischargeEfficiency() {
        return dischargeEfficiency;
    }

    @Override
    public String toString() {
        return getTechnologyName() + " [name=" + name + ", capacity=" + capacityKwh + " kWh, SOC="
                + String.format("%.2f", socPercent) + "%]";
    }
}

