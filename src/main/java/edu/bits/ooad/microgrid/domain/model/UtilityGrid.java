package edu.bits.ooad.microgrid.domain.model;

/**
 * Represents the external utility grid connection at the Point of Common Coupling (PCC).
 * Tracks grid availability, feeder import limits, and export limits.
 */
public class UtilityGrid {

    private boolean available;
    private double maxImportKw;
    private double maxExportKw;

    public UtilityGrid(boolean available, double maxImportKw, double maxExportKw) {
        if (maxImportKw < 0.0) {
            throw new IllegalArgumentException("Max import limit must be non-negative, got " + maxImportKw);
        }
        if (maxExportKw < 0.0) {
            throw new IllegalArgumentException("Max export limit must be non-negative, got " + maxExportKw);
        }
        this.available = available;
        this.maxImportKw = maxImportKw;
        this.maxExportKw = maxExportKw;
    }

    public UtilityGrid(boolean available) {
        this(available, 100.0, 50.0);
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getMaxImportKw() {
        return available ? maxImportKw : 0.0;
    }

    public void setMaxImportKw(double maxImportKw) {
        if (maxImportKw < 0.0) {
            throw new IllegalArgumentException("Max import limit must be non-negative");
        }
        this.maxImportKw = maxImportKw;
    }

    public double getMaxExportKw() {
        return available ? maxExportKw : 0.0;
    }

    public void setMaxExportKw(double maxExportKw) {
        if (maxExportKw < 0.0) {
            throw new IllegalArgumentException("Max export limit must be non-negative");
        }
        this.maxExportKw = maxExportKw;
    }

    public double clampImport(double requestedKw) {
        if (!available || requestedKw <= 0.0) {
            return 0.0;
        }
        return Math.min(requestedKw, maxImportKw);
    }

    public double clampExport(double requestedKw) {
        if (!available || requestedKw <= 0.0) {
            return 0.0;
        }
        return Math.min(requestedKw, maxExportKw);
    }

    @Override
    public String toString() {
        return "UtilityGrid{available=" + available + ", maxImportKw=" + maxImportKw + ", maxExportKw="
                + maxExportKw + "}";
    }
}

