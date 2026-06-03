# 🏠 DomusControl

DomusControl is a Java console application that simulates a smart home automation system. It allows users to manage virtual houses, organize devices by division, define automations and schedules, run personal scenarios, simulate environmental conditions, and persist the full application state.

The project was developed for the Object-Oriented Programming course at Universidade do Minho and focuses on clean domain modelling, encapsulation, inheritance, polymorphism, interfaces, design patterns, serialization, and automated testing.

## Features

* Multi-user system with role-based access per house
* House and division management
* Support for multiple smart device types
* Environmental simulation with time, temperature, luminosity and weather
* Physical sensors connected to the simulated environment
* Automations triggered by device, sensor or time conditions
* Time-based schedules
* Personal scenarios composed of reusable actions
* Suggestion engine based on manual interaction history
* Usage and consumption statistics
* Binary persistence using Java serialization
* JUnit 5 test coverage for the main modules

## Architecture

The application follows a layered MVC Delegate approach. The console interface is separated from the domain model, and all external access to the model is centralized through the `DomusControl` facade.

```text
Console UI
Menu / DomusControlUI / UserUI / HouseUI / ActionsUI
        |
        v
DomusControl
Facade over the domain model
        |
        +-- UserManager
        +-- HouseManager
        +-- Simulation
```

This structure keeps the UI focused on interaction flow while the model contains the business rules and application state.

## Main Concepts

### Users and Houses

Users can be associated with multiple houses and have different roles in each one:

* `ADMINISTRATOR`: manages houses, divisions, devices and user access
* `USER`: operates devices and uses available house functionality

Each house groups devices by division and manages its own routines, scenarios, sensors and interaction history.

### Devices

Devices share a common abstract base class and are extended through more specific abstract classes and capability interfaces.

```text
Device
├── SwitchableDevice
│   ├── Relay
│   ├── Plug
│   └── AdjustableDevice
│       ├── Lamp
│       ├── Speaker
│       ├── Television
│       ├── Fan
│       ├── AirConditioner
│       └── Heater
└── OpenableDevice
    ├── Curtain
    └── Gate
```

Supported device categories include:

| Category   | Devices                                                    | Main capability                     |
| ---------- | ---------------------------------------------------------- | ----------------------------------- |
| Switchable | `Relay`, `Plug`                                            | Turn on and off                     |
| Adjustable | `Speaker`, `Television`, `Fan`, `AirConditioner`, `Heater` | Set level from `0` to `100`         |
| Light      | `Lamp`                                                     | Brightness and color temperature    |
| Openable   | `Curtain`, `Gate`                                          | Set opening level from `0` to `100` |
| Sensors    | `TemperatureSensor`, `LuminositySensor`, `RainfallSensor`  | Read simulation state               |

The `ColorAdjustableDevice` capability is implemented as an interface, allowing devices such as `Lamp` to support color temperature without forcing that behavior into the entire adjustable-device hierarchy.

### Simulation

The simulation module tracks the current virtual environment:

* Current and previous simulation time
* Outside temperature
* Luminosity
* Weather condition

Weather affects luminosity through predefined multipliers, and sensors read values from the simulation state whenever time advances.

### Routines

DomusControl supports three types of routines:

* **Automations**: execute actions when a set of conditions becomes true
* **Schedules**: automations restricted to time-based conditions
* **Scenarios**: named action sequences triggered manually by the user

Actions target devices by ID instead of storing direct object references. This keeps routines serializable, easier to clone, and safer when devices are removed from a house.

Examples of supported actions:

* Turn a device on or off
* Set an adjustable level
* Set an opening percentage
* Set a light color temperature

Examples of supported conditions:

* Device state
* Device level
* Opening level
* Time or time window
* Temperature sensor value
* Luminosity sensor value
* Rainfall sensor value

Automations only fire when their conditions transition from false to true, preventing repeated execution while a condition remains true across simulation ticks.

### Suggestions

Manual interactions are logged with contextual data such as user, device, timestamp, weather, temperature and luminosity. The suggestion engine analyses this history and proposes automations based on repeated patterns, including:

* Similar actions around the same time of day
* Device actions that often happen in sequence
* Actions repeated under certain temperature conditions
* Actions repeated when luminosity is low or high

## Persistence

Application state is saved using Java binary serialization. Loading a saved state restores users, houses, divisions, devices, routines, scenarios, sensors, interaction history and simulation data.

After loading, internal ID counters are restored to avoid collisions with newly created houses or devices.

## Statistics

The application provides statistics for analysing usage and consumption, including:

* Devices with the highest usage time
* Devices with the most activations
* House consumption data
* Divisions with the most devices
* Global and per-house summaries

## Getting Started

### Requirements

* Java JDK
* Gradle wrapper included in the repository

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew run --console=plain
```

### Run Tests

```bash
./gradlew test
```

## Project Structure

```text
src/
├── main/
│   └── java/
│       ├── devices/          # Device hierarchy and concrete devices
│       │   └── sensors/      # Environmental sensors
│       ├── simulation/       # Time, weather, luminosity and temperature
│       ├── houses/           # Houses, divisions and house-level operations
│       ├── routines/         # Automations, schedules, scenarios, actions and conditions
│       ├── suggestions/      # Interaction logging and automation suggestions
│       ├── user/             # Users, roles and authentication
│       ├── exceptions/       # Domain-specific exceptions
│       └── ui/               # Console interface
└── test/
    └── java/                 # JUnit 5 tests
```

## Authors

* [Matheus Azevedo](https://github.com/matheusm18)
* [Martim Monteiro](https://github.com/triplo3mmm)
* [Afonso Barros](https://github.com/barros-11)
