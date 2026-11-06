package edu.bits.ooad.microgrid.web;

import edu.bits.ooad.microgrid.application.MicrogridController;
import edu.bits.ooad.microgrid.application.Scenario;
import edu.bits.ooad.microgrid.application.SimulationEngine;
import edu.bits.ooad.microgrid.domain.factory.BatteryFactory;
import edu.bits.ooad.microgrid.domain.factory.BatteryType;
import edu.bits.ooad.microgrid.domain.model.Battery;
import edu.bits.ooad.microgrid.domain.model.Inverter;
import edu.bits.ooad.microgrid.domain.service.MonitoringService;
import edu.bits.ooad.microgrid.domain.strategy.EnergyManagementStrategy;
import edu.bits.ooad.microgrid.domain.strategy.PeakShavingStrategy;
import edu.bits.ooad.microgrid.domain.strategy.SelfConsumptionStrategy;
import edu.bits.ooad.microgrid.domain.strategy.StrategyType;
import edu.bits.ooad.microgrid.web.dto.SimulationConfigDto;
import edu.bits.ooad.microgrid.web.dto.SimulationStateDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller exposing microgrid simulation operations to the web dashboard.
 */
@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class MicrogridApiController {

    private final SimulationEngine simulationEngine;
    private final MicrogridController controller;
    private final MonitoringService monitoringService;
    private final BatteryFactory batteryFactory;
    private final edu.bits.ooad.microgrid.application.SampleFileGenerator sampleFileGenerator;

    public MicrogridApiController(
            SimulationEngine simulationEngine,
            MicrogridController controller,
            MonitoringService monitoringService,
            BatteryFactory batteryFactory,
            edu.bits.ooad.microgrid.application.SampleFileGenerator sampleFileGenerator) {
        this.simulationEngine = simulationEngine;
        this.controller = controller;
        this.monitoringService = monitoringService;
        this.batteryFactory = batteryFactory;
        this.sampleFileGenerator = sampleFileGenerator;
    }

    /**
     * Retrieves the current state, active configuration, and telemetry.
     */
    @GetMapping("/state")
    public SimulationStateDto getState() {
        return buildStateDto();
    }

    /**
     * Executes one simulation control step with user-specified duration in hours (e.g. 1.0 = 60 mins, 0.5 = 30 mins).
     */
    @PostMapping("/step")
    public SimulationStateDto executeStep(@RequestParam(defaultValue = "1.0") double stepHours) {
        simulationEngine.runStep(stepHours);
        return buildStateDto();
    }

    /**
     * Executes multiple consecutive simulation steps with user-specified duration.
     */
    @PostMapping("/run-steps")
    public SimulationStateDto executeSteps(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "1.0") double stepHours) {
        simulationEngine.runSteps(Math.max(1, Math.min(100, count)), stepHours);
        return buildStateDto();
    }

    /**
     * Runs a predefined simulation scenario profile.
     */
    @PostMapping("/run-scenario")
    public SimulationStateDto runScenario(@RequestParam(defaultValue = "sunny_day_24h") String scenarioId) {
        Scenario scenario = "grid_outage_resilience".equalsIgnoreCase(scenarioId)
                ? Scenario.createGridOutageProfile()
                : Scenario.createSunnyDayProfile();

        simulationEngine.runScenario(scenario);
        return buildStateDto();
    }

    /**
     * Simulates utility grid outage at the Point of Common Coupling.
     */
    @PostMapping("/grid-failure")
    public SimulationStateDto simulateGridFailure() {
        controller.simulateGridFailure();
        return buildStateDto();
    }

    /**
     * Simulates utility grid restoration and synchronization.
     */
    @PostMapping("/grid-restore")
    public SimulationStateDto simulateGridRestore() {
        controller.simulateGridRestore();
        return buildStateDto();
    }

    /**
     * Updates simulation configuration parameters.
     */
    @PostMapping("/configure")
    public SimulationStateDto configure(@RequestBody SimulationConfigDto config) {
        if (config.solarGenerationKw() != null || config.totalDemandKw() != null || config.criticalDemandKw() != null) {
            double solar = (config.solarGenerationKw() != null)
                    ? config.solarGenerationKw()
                    : controller.getSolarPV().getAvailablePowerKw();
            double total = (config.totalDemandKw() != null)
                    ? config.totalDemandKw()
                    : controller.getLoad().getTotalDemandKw();
            double critical = (config.criticalDemandKw() != null)
                    ? config.criticalDemandKw()
                    : controller.getLoad().getCriticalDemandKw();
            controller.updateInputs(solar, total, critical);
        }

        if (config.batteryType() != null || config.batteryCapacityKwh() != null || config.batterySocPercent() != null) {
            Battery current = controller.getBattery();
            BatteryType type = (config.batteryType() != null)
                    ? config.batteryType()
                    : BatteryType.fromString(current.getTechnologyName());
            double capacity = (config.batteryCapacityKwh() != null) ? config.batteryCapacityKwh() : current.getCapacityKwh();
            double soc = (config.batterySocPercent() != null) ? config.batterySocPercent() : current.getSocPercent();

            Battery newBattery = batteryFactory.createBattery(type, capacity, soc);
            controller.setBattery(newBattery);
        }

        if (config.inverterRatingKw() != null && config.inverterRatingKw() > 0.0) {
            // Reconfigure inverter
            Inverter inv = new Inverter(config.inverterRatingKw());
            // Replace inverter via reflection/field or direct constructor
        }

        if (config.strategyType() != null) {
            EnergyManagementStrategy strategy = (config.strategyType() == StrategyType.PEAK_SHAVING)
                    ? new PeakShavingStrategy(config.peakShavingTargetKw() != null ? config.peakShavingTargetKw() : 25.0)
                    : new SelfConsumptionStrategy();
            controller.setStrategy(strategy);
        }

        if (config.peakShavingTargetKw() != null) {
            controller.setPeakShavingTargetKw(config.peakShavingTargetKw());
        }

        if (config.gridAvailable() != null) {
            if (config.gridAvailable()) {
                controller.simulateGridRestore();
            } else {
                controller.simulateGridFailure();
            }
        }

        if (config.gridMaxImportKw() != null) {
            controller.getUtilityGrid().setMaxImportKw(config.gridMaxImportKw());
        }
        if (config.gridMaxExportKw() != null) {
            controller.getUtilityGrid().setMaxExportKw(config.gridMaxExportKw());
        }

        return buildStateDto();
    }

    /**
     * Resets the simulation state and telemetry history.
     */
    @PostMapping("/reset")
    public SimulationStateDto reset(@RequestParam(defaultValue = "50.0") double initialSoc) {
        simulationEngine.reset(initialSoc);
        return buildStateDto();
    }

    /**
     * Exports the recorded simulation steps as a CSV file.
     */
    @GetMapping(value = "/export-csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        String csvData = monitoringService.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"h2025H1030061P_D2_output.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    /**
     * Exports the standard 24-hour diurnal input scenario as an Excel-compatible CSV file.
     */
    @GetMapping(value = "/export-input-csv", produces = "text/csv")
    public ResponseEntity<String> exportInputCsv() {
        String csvData = sampleFileGenerator.getInputCsvContent();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"h2025H1030061P_D2_input.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    private SimulationStateDto buildStateDto() {
        Battery battery = controller.getBattery();
        return new SimulationStateDto(
                controller.getStepIndex(),
                controller.getGridState().getStateName(),
                controller.isGridPhysicallyAvailable(),
                controller.getStrategy().getStrategyName(),
                controller.getStrategy().getStrategyType().name(),
                controller.getSolarPV().getAvailablePowerKw(),
                controller.getLoad().getTotalDemandKw(),
                controller.getLoad().getCriticalDemandKw(),
                battery.getName(),
                battery.getTechnologyName(),
                battery.getSocPercent(),
                battery.getMinSocPercent(),
                battery.getMaxSocPercent(),
                battery.getCapacityKwh(),
                controller.getInverter().getRatedPowerKw(),
                controller.getPeakShavingTargetKw(),
                monitoringService.getLatestResult(),
                monitoringService.getHistory(),
                monitoringService.getTransitionLogs(),
                monitoringService.getTotalSolarGeneratedKwh(),
                monitoringService.getTotalSolarConsumedKwh(),
                monitoringService.getTotalSolarExportedKwh(),
                monitoringService.getTotalGridImportedKwh(),
                monitoringService.getTotalLoadServedKwh(),
                monitoringService.getTotalUnmetLoadKwh(),
                monitoringService.getTotalUnmetCriticalLoadKwh(),
                monitoringService.getTotalSimulatedHours());
    }
}

