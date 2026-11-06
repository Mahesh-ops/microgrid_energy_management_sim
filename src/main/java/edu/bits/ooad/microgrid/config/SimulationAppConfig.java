package edu.bits.ooad.microgrid.config;

import edu.bits.ooad.microgrid.application.MicrogridController;
import edu.bits.ooad.microgrid.application.SimulationEngine;
import edu.bits.ooad.microgrid.domain.factory.BatteryFactory;
import edu.bits.ooad.microgrid.domain.service.MonitoringService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration defining application beans.
 */
@Configuration
public class SimulationAppConfig {

    @Bean
    public BatteryFactory batteryFactory() {
        return new BatteryFactory();
    }

    @Bean
    public MonitoringService monitoringService() {
        return new MonitoringService();
    }

    @Bean
    public MicrogridController microgridController() {
        return MicrogridController.createDefault();
    }

    @Bean
    public SimulationEngine simulationEngine(MicrogridController controller, MonitoringService monitoringService) {
        return new SimulationEngine(controller, monitoringService);
    }
}

