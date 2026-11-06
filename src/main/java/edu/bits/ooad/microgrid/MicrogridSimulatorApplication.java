package edu.bits.ooad.microgrid;

import edu.bits.ooad.microgrid.application.SampleFileGenerator;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MicrogridSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicrogridSimulatorApplication.class, args);
	}

	@Bean
	public ApplicationRunner sampleFileRunner(SampleFileGenerator generator) {
		return args -> generator.generateAllSampleFiles();
	}

}
