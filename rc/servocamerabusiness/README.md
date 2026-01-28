# servocamerabusiness

## Description
This module controls a 2-axis camera mounted on servos. It provides high-level operations to rotate the horizontal and vertical axes, convert axis input values to servo pulse values (PWM) and apply them via the servocameradriver (`PWMControler`).

## Key components
`ServoCameraBusiness`: main class that manages servo positions, movement mode and converts axis input values into servo pulse deltas or absolute pulse values.

Important behaviours:
- Two movement modes: incremental (0) and absolute (1).
- Bounds checking on pulse values before applying them to hardware via `PWMControler`.
- Helper methods: `rotationAxeHori`, `rotationAxeVert`, `resetPosition`, `switchMode`, and `AxisToPulse`.

Driver dependency:
- `fr.ensma.a3.ia.servocameradriver.PWMControler` — applies pulse values to the physical PWM controller.

Important parameters (from code)
- `idPWMHoriz` / `idPWMVert` — PWM channel IDs (defaults 0 and 1).
- `modeDeplacement` — movement mode: 0 = incremental, 1 = absolute.
- `maxAxe0` / `minAxe0` — horizontal servo pulse bounds (1900 / 1100).
- `maxAxe1` / `minAxe1` — vertical servo pulse bounds (1900 / 1100).
- `centerAxe0` / `centerAxe1` — center pulses (1500 / 1600).
- `servo0Position` / `servo1Position` — current applied pulse values for horizontal and vertical servos.
- `AxisToPulse(int x)` — converts an axis input (expected range -100..100) into a pulse delta or absolute pulse depending on mode.

AxisToPulse behaviour
- Quantizes input by steps of 20, then scales to pulse units.
- In absolute mode it applies additional scaling (as implemented in code).

## Build and run
- Build modules:

```bash
mvn -f servocameradriver/pom.xml clean install -DskipTests
mvn -f servocamerabusiness/pom.xml clean package -DskipTests
```

- Run a consumer that uses `ServoCameraBusiness` (replace with your actual main class):

```bash
mvn -f servocamerabusiness/pom.xml exec:java -Dexec.mainClass=fr.ensma.a3.ia.servocamerabusiness.ServoCameraBusiness
```

Runtime notes:
- On x86_64 development machines the driver module may fall back to a MOCK mode to avoid native provider errors; in mock mode the business logic runs but no real PWM is applied.
- For real hardware tests run on a Raspberry Pi (or equivalent) with the correct PWM provider / Pi4J setup so `PWMControler` can access PWM hardware.

## Usage examples
- Instantiate with defaults:
```java
ServoCameraBusiness cam = new ServoCameraBusiness();
```
- Instantiate with explicit PWM channels:
```java
ServoCameraBusiness cam = new ServoCameraBusiness(0, 1);
```
- Incremental horizontal move (val ∈ -100..100):
```java
cam.rotationAxeHori(50);
```
- Switch between incremental and absolute modes:
```java
cam.switchMode();
```

## Authors and acknowledgment
SAUX Lisa & CHARBONNEL Adrien in the BE of M. Richard and M. Baron.

## Project status
Finished.