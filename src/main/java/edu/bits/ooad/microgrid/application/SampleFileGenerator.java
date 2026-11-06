package edu.bits.ooad.microgrid.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Service that automatically generates Excel-compatible CSV and standard submission
 * files for both input scenario profiles and simulation output telemetry.
 */
@Service
public class SampleFileGenerator {

    private static final Logger log = LoggerFactory.getLogger(SampleFileGenerator.class);

    /**
     * Generates all input and output files (both .csv and .ip1/.op1 formats).
     */
    public void generateAllSampleFiles() {
        generateInputFiles();
        generateOutputFiles();
    }

    /**
     * Generates input scenario CSV and .ip1 files based on the 24-hour diurnal profile.
     */
    public void generateInputFiles() {
        Scenario scenario = Scenario.createSunnyDayProfile();

        // 1. Build Excel-friendly CSV format matching simulation inputs
        StringBuilder csv = new StringBuilder();
        csv.append("Hour,SolarGen_kW,TotalDemand_kW,CriticalDemand_kW,GridAvailable\n");

        for (ScenarioStep step : scenario.getSteps()) {
            csv.append(String.format("%d,%.2f,%.2f,%.2f,%s\n",
                    step.hour(),
                    step.solarGenerationKw(),
                    step.totalDemandKw(),
                    step.criticalDemandKw(),
                    step.gridAvailable()));
        }

        String inputCsvContent = csv.toString();
        writeFile("h2025H1030061P_D2_input.csv", inputCsvContent);
        writeFile("h2025H1030061P_D2.ip1.csv", inputCsvContent);

        // 2. Build structured .ip1 format
        StringBuilder ip1 = new StringBuilder();
        ip1.append("# Microgrid Simulation Sample Input File (.ip1)\n");
        ip1.append("# Candidate: Jalluri Mahesh Kumar | ID: 2025H1030061P\n");
        ip1.append("# Course: SS G514 (Object Oriented Analysis & Design)\n");
        ip1.append("# Area 20: Electrical Science - Single-Controllable Microgrid Implementation\n\n");
        ip1.append("[MICROGRID_CONFIGURATION]\n");
        ip1.append("solar_rated_capacity_kw = 100.0\n");
        ip1.append("inverter_rated_power_kw = 60.0\n");
        ip1.append("battery_technology = LITHIUM_ION\n");
        ip1.append("battery_capacity_kwh = 100.0\n");
        ip1.append("battery_initial_soc_percent = 50.0\n");
        ip1.append("grid_feeder_import_limit_kw = 100.0\n");
        ip1.append("grid_feeder_export_limit_kw = 50.0\n");
        ip1.append("supervisory_strategy = SELF_CONSUMPTION\n");
        ip1.append("peak_shaving_target_kw = 25.0\n\n");
        ip1.append("[HOURLY_SIMULATION_PROFILE_24H]\n");
        ip1.append("# Hour, SolarGen_kW, TotalLoad_kW, CriticalLoad_kW, GridAvailable\n");

        for (ScenarioStep step : scenario.getSteps()) {
            ip1.append(String.format("Hour_%02d = %.1f, %.1f, %.1f, %s\n",
                    step.hour(), step.solarGenerationKw(), step.totalDemandKw(), step.criticalDemandKw(), step.gridAvailable()));
        }

        writeFile("h2025H1030061P_D2.ip1", ip1.toString());
        log.info("Successfully generated sample input files: h2025H1030061P_D2_input.csv, h2025H1030061P_D2.ip1.csv, h2025H1030061P_D2.ip1");
    }

    /**
     * Executes the 24-hour diurnal scenario and generates output CSV and .op1 files.
     */
    public void generateOutputFiles() {
        SimulationEngine engine = SimulationEngine.createDefault();
        engine.runScenario(Scenario.createSunnyDayProfile());
        String outputCsvContent = engine.getMonitoringService().exportCsv();

        writeFile("h2025H1030061P_D2_output.csv", outputCsvContent);
        writeFile("h2025H1030061P_D2.op1.csv", outputCsvContent);
        writeFile("h2025H1030061P_D2.op1", outputCsvContent);

        log.info("Successfully generated sample output files: h2025H1030061P_D2_output.csv, h2025H1030061P_D2.op1.csv, h2025H1030061P_D2.op1");
    }

    public String getInputCsvContent() {
        Scenario scenario = Scenario.createSunnyDayProfile();
        StringBuilder csv = new StringBuilder();
        csv.append("Hour,SolarGen_kW,TotalDemand_kW,CriticalDemand_kW,GridAvailable\n");
        for (ScenarioStep step : scenario.getSteps()) {
            csv.append(String.format("%d,%.2f,%.2f,%.2f,%s\n",
                    step.hour(), step.solarGenerationKw(), step.totalDemandKw(), step.criticalDemandKw(), step.gridAvailable()));
        }
        return csv.toString();
    }

    private void writeFile(String filename, String content) {
        try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            log.error("Failed to write sample file {}: {}", filename, e.getMessage());
        }
    }
}
