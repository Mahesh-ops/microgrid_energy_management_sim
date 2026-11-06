package edu.bits.ooad.microgrid.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MicrogridApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/simulation/state should return initial system state")
    void testGetState() throws Exception {
        mockMvc.perform(get("/api/simulation/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operatingState").exists())
                .andExpect(jsonPath("$.strategyName").value("Self-Consumption"));
    }

    @Test
    @DisplayName("POST /api/simulation/step should execute one step and advance step count")
    void testExecuteStep() throws Exception {
        mockMvc.perform(post("/api/simulation/step"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stepIndex").isNumber());
    }

    @Test
    @DisplayName("POST /api/simulation/step?stepHours=0.5 should record stepHours in history and latestResult")
    void testStepHoursInResponse() throws Exception {
        mockMvc.perform(post("/api/simulation/step").param("stepHours", "0.5"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestResult.stepHours").value(0.5));
    }

    @Test
    @DisplayName("POST /api/simulation/grid-failure should transition state to Islanded")
    void testGridFailureApi() throws Exception {
        mockMvc.perform(post("/api/simulation/grid-failure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operatingState").value("Islanded"));
    }

    @Test
    @DisplayName("POST /api/simulation/grid-restore should reconnect to grid")
    void testGridRestoreApi() throws Exception {
        // Ensure islanded first
        mockMvc.perform(post("/api/simulation/grid-failure"));

        mockMvc.perform(post("/api/simulation/grid-restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operatingState").value("Grid-Connected"));
    }

    @Test
    @DisplayName("GET /api/simulation/export-csv should return downloadable CSV data")
    void testExportCsv() throws Exception {
        // Run a step first
        mockMvc.perform(post("/api/simulation/step"));

        mockMvc.perform(get("/api/simulation/export-csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Step,State,Strategy")));
    }

    @Test
    @DisplayName("GET /api/simulation/export-input-csv should return downloadable input scenario CSV")
    void testExportInputCsv() throws Exception {
        mockMvc.perform(get("/api/simulation/export-input-csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hour,SolarGen_kW,TotalDemand_kW")));
    }
}
