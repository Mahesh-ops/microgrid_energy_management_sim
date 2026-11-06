package edu.bits.ooad.microgrid.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Predefined simulation scenario containing a sequence of hourly inputs.
 */
public class Scenario {

    private final String id;
    private final String name;
    private final String description;
    private final List<ScenarioStep> steps;

    public Scenario(String id, String name, String description, List<ScenarioStep> steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.steps = (steps != null) ? List.copyOf(steps) : Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ScenarioStep> getSteps() {
        return steps;
    }

    /**
     * Creates a standard 24-hour Diurnal Profile (Sunny Day with Evening Peak).
     */
    public static Scenario createSunnyDayProfile() {
        List<ScenarioStep> steps = new ArrayList<>();
        // 24-hour curve: solar rises around 06:00, peaks at 12:00 (85 kW), sets by 18:00
        // load has morning rise (40 kW), mid-day (50 kW), evening peak at 19:00 (75 kW)
        double[] solarProfile = {
                0, 0, 0, 0, 0, 5, 18, 42, 68, 80, 85, 85, 82, 70, 50, 28, 10, 0, 0, 0, 0, 0, 0, 0
        };
        double[] loadProfile = {
                20, 18, 18, 19, 22, 28, 38, 45, 48, 50, 52, 50, 48, 46, 45, 52, 60, 72, 78, 75, 62, 45, 30, 24
        };

        for (int h = 0; h < 24; h++) {
            double solar = solarProfile[h];
            double load = loadProfile[h];
            double critical = load * 0.35; // 35% critical load
            String desc = String.format("Hour %02d:00 - Solar: %.1f kW, Load: %.1f kW", h, solar, load);
            steps.add(new ScenarioStep(h + 1, solar, load, critical, true, desc));
        }

        return new Scenario(
                "sunny_day_24h",
                "24-Hour Diurnal Solar & Load Cycle",
                "Realistic full-day simulation with afternoon solar peak and evening residential/commercial demand peak.",
                steps);
    }

    /**
     * Creates a Grid Outage & Resilience Scenario (Grid fails between Hour 12 and 16).
     */
    public static Scenario createGridOutageProfile() {
        List<ScenarioStep> steps = new ArrayList<>();
        double[] solarProfile = {10, 30, 60, 75, 70, 50, 30, 10, 0, 0};
        double[] loadProfile = {40, 45, 50, 60, 65, 70, 60, 50, 40, 35};

        for (int h = 0; h < 10; h++) {
            int stepNum = h + 1;
            boolean gridAvailable = !(stepNum >= 4 && stepNum <= 7); // Outage during hours 4 to 7
            double solar = solarProfile[h];
            double load = loadProfile[h];
            double critical = 25.0;
            String desc = gridAvailable ? "Grid Normal" : "GRID OUTAGE - Islanded Operation";
            steps.add(new ScenarioStep(stepNum, solar, load, critical, gridAvailable, desc));
        }

        return new Scenario(
                "grid_outage_resilience",
                "Grid Outage & Islanded Resilience",
                "Simulates sudden feeder loss during peak hours, forcing transition to islanded state and critical load prioritization.",
                steps);
    }
}

