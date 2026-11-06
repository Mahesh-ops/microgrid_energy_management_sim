package edu.bits.ooad.microgrid.domain.model;

/**
 * Represents a solar photovoltaic (PV) generation system.
 * Encapsulates generation capacity, current power availability, and validation.
 */
public class SolarPV {

    private final double capacityKw;
    private double currentGenerationKw;

    public SolarPV(double capacityKw, double currentGenerationKw) {
        if (capacityKw < 0.0) {
            throw new IllegalArgumentException("Solar capacity must be non-negative, got " + capacityKw);
        }
        if (currentGenerationKw < 0.0) {
            throw new IllegalArgumentException("Solar generation must be non-negative, got " + currentGenerationKw);
        }
        if (currentGenerationKw > capacityKw) {
            throw new IllegalArgumentException(
                    "Solar generation (" + currentGenerationKw + " kW) cannot exceed capacity (" + capacityKw + " kW)");
        }
        this.capacityKw = capacityKw;
        this.currentGenerationKw = currentGenerationKw;
    }

    public SolarPV(double capacityKw) {
        this(capacityKw, 0.0);
    }

    public double getCapacityKw() {
        return capacityKw;
    }

    public double getCurrentGenerationKw() {
        return currentGenerationKw;
    }

    public void setGeneration(double generationKw) {
        if (generationKw < 0.0) {
            throw new IllegalArgumentException("Solar generation cannot be negative: " + generationKw);
        }
        if (generationKw > capacityKw) {
            throw new IllegalArgumentException(
                    "Solar generation " + generationKw + " kW exceeds rated capacity " + capacityKw + " kW");
        }
        this.currentGenerationKw = generationKw;
    }

    public double getAvailablePowerKw() {
        return currentGenerationKw;
    }

    @Override
    public String toString() {
        return "SolarPV{capacityKw=" + capacityKw + ", currentGenerationKw=" + currentGenerationKw + "}";
    }
}

