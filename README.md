# Microgrid Energy Management Simulator

**Course:** SS G514 — Object Oriented Analysis & Design (BITS Pilani)  
**Assigned Area:** Area 20 — Electrical Science  
**Candidate Name:** Jalluri Mahesh Kumar  
**ID Number:** 2025H1030061P (Higher Degree)  
**Case Study Reference:** *G. L. Reis, D. I. Brandão, J. H. Oliveira, L. S. Araújo, and B. J. Cardoso Filho, "Case Study of Single-Controllable Microgrid: A Practical Implementation," Energies, vol. 15, article 6400, 2022.*

---

## 1. Project Overview

The **Microgrid Energy Management Simulator** is an educational, object-oriented software application engineered in **Java 24** and **Spring Boot 4**. It models the supervisory coordination of a single-controllable microgrid consisting of:
- **Solar Photovoltaic (PV)** generation array;
- **Multi-chemistry Battery Energy Storage System (BESS)** (Lithium-Ion, Lead-Acid, Sodium-Nickel);
- **Bidirectional Inverter / Power Conversion System (PCS)**;
- **Critical & Non-Critical Electrical Loads**; and
- **Utility Grid Connection** at the Point of Common Coupling (PCC).

The primary objective is to demonstrate core **Object-Oriented Analysis & Design (OOAD)** concepts, **SOLID principles**, and **GoF design patterns** through a clean, testable cyber-physical energy simulation.

---

## 2. Design Patterns Implemented

| Pattern | Interface / Base Class | Concrete Implementations | Purpose & Role |
|---|---|---|---|
| **Strategy** | `EnergyManagementStrategy` | `SelfConsumptionStrategy`, `PeakShavingStrategy` | Decouples energy dispatch optimization from the controller. Strategies can be switched dynamically at runtime. |
| **Factory Method** | `BatteryFactory` | `LithiumIonBattery`, `LeadAcidBattery`, `SodiumNickelBattery` | Encapsulates instantiation and chemical parameter defaults (SOC bounds, continuous C-rates, efficiencies) behind the common `Battery` abstraction. |
| **State** | `GridState` | `GridConnectedState`, `IslandedState` | Encapsulates operating mode behavior and transitions (grid exchange rules, blackout response, simulated reconnection guard). |
| **Observer** | `SimulationSubject` / `SimulationObserver` | `MicrogridController` (Publisher) $\rightarrow$ `MonitoringService` (Subscriber) | Decouples telemetry recording, energy KPI accumulation, and CSV export from the control loop. |

---

## 3. System Architecture & SOLID Conformance

```
edu.bits.ooad.microgrid.web
    | (REST API / DTOs)
    v
edu.bits.ooad.microgrid.application
    | (MicrogridController, SimulationEngine)
    v
edu.bits.ooad.microgrid.domain
    |-- model      (SolarPV, Load, Inverter, UtilityGrid, Battery)
    |-- strategy   (SelfConsumptionStrategy, PeakShavingStrategy)
    |-- state      (GridConnectedState, IslandedState)
    |-- factory    (BatteryFactory, BatteryType)
    |-- service    (PlanValidator, MonitoringService)
    |-- observer   (SimulationObserver, SimulationSubject)
```

- **Single Responsibility (SRP):** `PlanValidator` only checks physical safety; `Battery` only manages electrochemical SOC; `MonitoringService` only records telemetry.
- **Open/Closed (OCP):** New strategies or battery chemistries can be added by implementing interfaces without editing controller dispatch loops.
- **Liskov Substitution (LSP):** All battery subclasses strictly adhere to the `Battery` contract, units (kW, kWh), and non-negative power bounds.
- **Interface Segregation (ISP):** Client code depends only on focused interfaces (`GridContext`, `SimulationObserver`).
- **Dependency Inversion (DIP):** `MicrogridController` depends on abstractions (`EnergyManagementStrategy`, `GridState`) rather than concrete classes.

---

## 4. How to Run & View Demonstration

### Prerequisites
- Java JDK 21 or higher installed on system PATH.

### 4.1 Running the Server
In PowerShell from this directory:
```powershell
.\mvnw.cmd spring-boot:run
```
Or run the packaged JAR directly:
```powershell
java -jar target\microgrid-simulator-0.0.1-SNAPSHOT.jar
```

### 4.2 Opening the Dashboard
Open your web browser and navigate to:
👉 **`http://localhost:8080/`**

The interactive dashboard features:
- **Reactive Two-Way Sliders:** All sliders (Solar, Demand, Critical Load, Battery Capacity, SOC) update dynamically as simulation steps proceed.
- **Configurable Step Duration:** Select 1 hour, 45 min, 30 min, 15 min, 10 min, 5 min, or a custom minute duration with live power-to-energy integration ($\Delta t$).
- **Live Auto-Advance:** Continuous hands-free stepping with adjustable pace (0.5s to 3.0s).
- **Elapsed Simulation Clock:** Real-time physical time tracking in the top header.
- **Interactive Charts:** Dual Chart.js visualizations for 6-channel power dispatch and SOC trends.
- **One-Click CSV Exports:** Direct download of input profile (`h2025H1030061P_D2_input.csv`) and output telemetry (`h2025H1030061P_D2_output.csv`).

### 4.3 Running Automated Tests
To run all 46 automated tests (unit tests, REST API tests, file generation tests, and Acceptance Scenarios T1 to T12):
```powershell
.\mvnw.cmd test
```

---

## 5. Deliverable D2 Submission Package Files

- **`h2025H1030061P_D2.docx`**: Full Final Report (10 sections with embedded UML diagrams matching BITS Pilani syllabus).
- **`h2025H1030061P_D2_Demo_Guide.docx`**: Demonstration manual, OO language details, and system requirements.
- **`h2025H1030061P_D2_input.csv` / `h2025H1030061P_D2.ip1.csv` / `h2025H1030061P_D2.ip1`**: Sample input scenario profiles (Excel-viewable CSV and standard format).
- **`h2025H1030061P_D2_output.csv` / `h2025H1030061P_D2.op1.csv` / `h2025H1030061P_D2.op1`**: Sample simulation output telemetry (Excel-viewable CSV and standard format).

