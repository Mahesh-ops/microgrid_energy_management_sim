package edu.bits.ooad.microgrid;

import edu.bits.ooad.microgrid.application.SampleFileGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleOutputGeneratorTest {

    @Test
    void generateSampleOutput() {
        SampleFileGenerator generator = new SampleFileGenerator();
        generator.generateAllSampleFiles();

        assertTrue(new File("h2025H1030061P_D2_input.csv").exists(), "Input CSV should exist");
        assertTrue(new File("h2025H1030061P_D2.ip1.csv").exists(), "Input .ip1.csv should exist");
        assertTrue(new File("h2025H1030061P_D2.ip1").exists(), "Input .ip1 should exist");

        assertTrue(new File("h2025H1030061P_D2_output.csv").exists(), "Output CSV should exist");
        assertTrue(new File("h2025H1030061P_D2.op1.csv").exists(), "Output .op1.csv should exist");
        assertTrue(new File("h2025H1030061P_D2.op1").exists(), "Output .op1 should exist");

        System.out.println("Verified automated generation of Excel-compatible CSV input & output files!");
    }
}

