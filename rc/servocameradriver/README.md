# Servo Camera Driver

## Description
This module implements a PCA9685-based PWM driver used to control servos for the camera mount of the RC car. It uses Pi4J for I2C access and exposes a simple singleton API to set PWM frequency and servo pulse widths. Intended to run on a Raspberry Pi (I2C bus 1) but can be used on any platform with Pi4J Linux I2C provider.

This project is the Driver part of the implementation.

## Key classes / API
`fr.ensma.a3.ia.servocameradriver.PWMControler` (singleton)
- `public static PWMControler getInstance()` — obtain the default singleton (auto-initializes Pi4J).
- `public static PWMControler getInstance(Context pi4j, int address)` — obtain singleton with provided Pi4J Context and custom I2C address.
- `public void write(int reg, int value)` — write a byte value to a PCA9685 register.
- `public int read(int reg)` — read a byte value from a PCA9685 register.
- `public void setPWMFreq(double freq)` — set the PWM frequency (Hz).
- `public void setServoPulse(int channel, int pulseMicroseconds)` — set servo pulse width (in µs) for a channel (0..15).

Notes:
- Default I2C provider: linuxfs-i2c (Pi4J).
- Default PCA9685 I2C address: 0x40.
- Default I2C bus: 1.
- Channels: 0..15 (16 channels).

## Installation
Build and install the module to the local Maven repository:

```bash
# from project root
mvn -f servocameradriver/pom.xml clean install -DskipTests
```

## Usage
Example (Java):

```java
import fr.ensma.a3.ia.servocameradriver.PWMControler;
import com.pi4j.context.Context;
import com.pi4j.Pi4J;

// default instance (auto-initializes Pi4J)
PWMControler pwm = PWMControler.getInstance();

// set frequency to 50 Hz (standard for servos)
pwm.setPWMFreq(50.0);

// set servo on channel 0 to 1500 µs (center)
pwm.setServoPulse(0, 1500);
```

If you need a custom I2C address or already manage a Pi4J Context:

```java
Context pi4j = Pi4J.newAutoContext();
PWMControler pwm = PWMControler.getInstance(pi4j, 0x40);
```

## Configuration & hardware notes
- Default I2C address: 0x40 (change via getInstance(Context, address)).
- I2C bus: 1 (Raspberry Pi).
- Ensure I2C is enabled on the Raspberry Pi (raspi-config -> Interface Options -> I2C).
- Power servos from a proper 5V supply; do not power them from the Pi 5V rail if current draw is high.
- Servo pulse calculations assume 50 Hz (20 ms period). Adjust setPWMFreq if using a different frequency.

## Running on a Raspberry Pi
Run your app with appropriate permissions if required (sudo may be needed depending on I2C permissions):

```bash
# from project root, example running a business module that uses the driver
mvn -f <your-app-module>/pom.xml exec:java -Dexec.mainClass=your.main.Class
```

## Authors and acknowledgment
SAUX Lisa & CHARBONNEL Adrien — supervision: M. Richard and M. Baron.

## Project status
Finished.
```# Servo Camera Driver

## Description
This module implements a PCA9685-based PWM driver used to control servos for the camera mount of the RC car. It uses Pi4J for I2C access and exposes a simple singleton API to set PWM frequency and servo pulse widths. Intended to run on a Raspberry Pi (I2C bus 1) but can be used on any platform with Pi4J Linux I2C provider.

Primary implementation: src/main/java/fr/ensma/a3/ia/servocameradriver/PWMControler.java

## Key classes / API
`fr.ensma.a3.ia.servocameradriver.PWMControler` (singleton)
- `public static PWMControler getInstance()` — obtain the default singleton (auto-initializes Pi4J).
- `public static PWMControler getInstance(Context pi4j, int address)` — obtain singleton with provided Pi4J Context and custom I2C address.
- `public void write(int reg, int value)` — write a byte value to a PCA9685 register.
- `public int read(int reg)` — read a byte value from a PCA9685 register.
- `public void setPWMFreq(double freq)` — set the PWM frequency (Hz).
- `public void setServoPulse(int channel, int pulseMicroseconds)` — set servo pulse width (in µs) for a channel (0..15).

Notes:
- Default I2C provider: linuxfs-i2c (Pi4J).
- Default PCA9685 I2C address: 0x40.
- Default I2C bus: 1.
- Channels: 0..15 (16 channels).

## Installation
Build and install the module to the local Maven repository:

```bash
# from project root
mvn -f servocameradriver/pom.xml clean install -DskipTests
```

## Usage
Example (Java):

```java
import fr.ensma.a3.ia.servocameradriver.PWMControler;
import com.pi4j.context.Context;
import com.pi4j.Pi4J;

// default instance (auto-initializes Pi4J)
PWMControler pwm = PWMControler.getInstance();

// set frequency to 50 Hz (standard for servos)
pwm.setPWMFreq(50.0);

// set servo on channel 0 to 1500 µs (center)
pwm.setServoPulse(0, 1500);
```

If you need a custom I2C address or already manage a Pi4J Context:

```java
Context pi4j = Pi4J.newAutoContext();
PWMControler pwm = PWMControler.getInstance(pi4j, 0x40);
```

## Configuration & hardware notes
- Default I2C address: 0x40 (change via getInstance(Context, address)).
- I2C bus: 1 (Raspberry Pi).
- Ensure I2C is enabled on the Raspberry Pi (raspi-config -> Interface Options -> I2C).
- Power servos from a proper 5V supply; do not power them from the Pi 5V rail if current draw is high.
- Servo pulse calculations assume 50 Hz (20 ms period). Adjust setPWMFreq if using a different frequency.

## Running on a Raspberry Pi
Run your app with appropriate permissions if required (sudo may be needed depending on I2C permissions):

```bash
# from project root, example running a business module that uses the driver
mvn -f <your-app-module>/pom.xml exec:java -Dexec.mainClass=your.main.Class
```

## Authors and acknowledgment
SAUX Lisa & CHARBONNEL Adrien — supervision: M. Richard and M. Baron.

## Project status
Finished.