package edu.bits.ooad.microgrid.domain.model;

/**
 * Represents electrical demand within the microgrid.
 * Loads are categorized into critical demand (essential circuits that must be
 * preserved during islanded operation) and non-critical demand.
 */
public class Load {

    private double totalDemandKw;
    private double criticalDemandKw;

    public Load(double totalDemandKw, double criticalDemandKw) {
        validate(totalDemandKw, criticalDemandKw);
        this.totalDemandKw = totalDemandKw;
        this.criticalDemandKw = criticalDemandKw;
    }

    public Load(double totalDemandKw) {
        this(totalDemandKw, 0.0);
    }

    private void validate(double total, double critical) {
        if (total < 0.0) {
            throw new IllegalArgumentException("Total demand must be non-negative, got " + total);
        }
        if (critical < 0.0) {
            throw new IllegalArgumentException("Critical demand must be non-negative, got " + critical);
        }
        if (critical > total) {
            throw new IllegalArgumentException(
                    "Critical demand (" + critical + " kW) cannot exceed total demand (" + total + " kW)");
        }
    }

    public double getTotalDemandKw() {
        return totalDemandKw;
    }

    public double getCriticalDemandKw() {
        return criticalDemandKw;
    }

    public double getNonCriticalDemandKw() {
        return Math.max(0.0, totalDemandKw - criticalDemandKw);
    }

    public void update(double totalDemandKw, double criticalDemandKw) {
        validate(totalDemandKw, criticalDemandKw);
        this.totalDemandKw = totalDemandKw;
        this.criticalDemandKw = criticalDemandKw;
    }

    @Override
    public String toString() {
        return "Load{totalDemandKw=" + totalDemandKw + ", criticalDemandKw=" + criticalDemandKw + "}";
    }
}

