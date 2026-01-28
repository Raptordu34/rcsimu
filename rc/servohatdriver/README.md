# Driver Control

## Java

```bash
mvn clean install
```
## TODO

- Watchdog for when network connection ends --> panama.brake
- Boot launch (see below)
- gpio reading pwm rc receptor (see below)
- Make c-library thread-safe in case controlled by simulator / rc receptor at same time

## Hardware setup

- Raspberry PI 4 connected to network & car battery **(careful voltage : 3.3V for Raspberry, use a buck or voltage divider)**
- PWM HAT PCA9685 connected to Raspberry
- ESC connected to channel 1 PWM of hat
- Servo connected to channel 0 PWM of hat

- Raspberry connected with ssh :
- username : thais@192.168.196.13
- password : bg

## How it works

### Components

 * - ESC : LRP Variator AI Runner Reverse V2.0 83020 
 *   https://www.lrp.cc/fileadmin/product_downloads/instructions_en/83020_en.pdf
 * - Servo motor : MG90S
 *   https://dosya.motorobit.com/pdf/MG90S_Tower-Pro.pdf
 * - PCA9685 manual Driver HAT 
 *   https://www.nxp.com/docs/en/data-sheet/PCA9685.pdf
 *   registers can be found in the documentation of the servo driver

### How commands are sent to the driver (PCA9685)

**The program cmd.c is in src/main/native.**

#### Initialization

First, **I²C** connection is made. The Raspberry PI setup a master I²C bus, and connects the HAT by his address (commonly found in the manual).

Then the module is reset, and the PWM frequency is set to 50Hz (usual frequency for servos).

Note : All registers and setup config, formulas can be found in the PCA9685 manual.

#### Send PWM signal

The module has 16 channels, from 0 to 15.

The I²C communication protocol is used to transmit bytes.

As shown in the manual, the HAT provides a default resolution of PWM signal which is **12 bits**, that means the value sent is between 0 & 4095. This is called the **duty**. More resolution means more precision for fine adjustments.

The **PWM signal** is created with a ON/OFF value. 
- ON : setting the PWM signal to HIGH
- OFF : setting the PWM signal to LOW

In our case, for servos, PWM signal begins by HIGH at 0 and ends with LOW at the duty, so **ON=0 & OFF=duty**.

The I²C can transmit only bytes (8 bits), so it is need to send 2 times the ON/OFF value, one for the 8-lasts bits and another for the 8 firsts-bits (begins with 0000 as it is 12-bits).

#### Conversion from duty to pulse in ms

As the frequency set is **50Hz**, each PWM cycle takes **20ms**.
If a duty like ON=0, OFF=409 is sent, that means the PWM signal is **HIGH from 0ms to 2ms, and LOW from 2ms to 20ms**.

#### Mappings

Mappings from percents to duty.

**Servo (direction)**

| angle_percent | PWM (pulse_angle) | Description | PWM (pulse ms) |
|:------------:|:----------------:|:-----------:|:----------------:|
| 0%           | 250              | 0°          |    1.22 ms       |
| 50%          | 345              | ~90°        |    1.68 ms       |
| 100%         | 440              | 180°        |    2.15 ms       |

**Motor (ESC)**

| speed_percent | PWM (pulse_motor) | Direction     | PWM (pulse ms)   |
|:------------:|:----------------:|:---------------:|:----------------:|
| +100%        | 155              | Full forward    | 0.76 ms          |
| 0%           | 420              | Stop            | 2.05 ms          |
| -100%        | 600              | Full backward   |  2.93 ms         |

PWM duties (pulses) can be changed in the cmd.c program.

## Git

```
cd existing_repo
git init
git remote add origin https://gitlab-ia.ensma.fr/be2025/driver.git
git branch -M main
git status
git log
git pull --rebase origin main
git push -uf origin main
```

## Upgrades

- Maybe the frequency, resolution can be adjusted for better tuning.
- The native library is not thread-safe, mutex could be implemented.
- Init function with channels tunable.
- uint8_t, inttypes could be used for better performance.
- Duty borders (pulses) tunable by functions, save configuration to file to reset as it was if reboot.

## Boot launch

```bash
sudo nano /etc/systemd/system/driver-test.service
```

```ini
[Unit]
Description=Driver Test Java Service
After=network.target

[Service]
Type=simple
User=thais
WorkingDirectory=/workspace
ExecStart=/usr/bin/java -cp target/DriverTest-1.0-SNAPSHOT.jar:target/dependances/* fr.ensma.a3.ia.driverTests.DriverExample
Restart=on-failure
RestartSec=5
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Reload systemd to apply the new service:

```bash
sudo systemctl daemon-reload
```

Enable the service to run at boot:

```bash
sudo systemctl enable driver-test.service
```

-----------------------------------------------

Start the service immediately (optional):

```bash
sudo systemctl start driver-test.service
```

Check the status/logs:

```bash
sudo systemctl status driver-test.service
sudo journalctl -u driver-test.service -f
```

## Control with the original rc controller

**IMPORTANT** : *pigpio* does not work on rpi5 !!!!

### Hardware setup

- B4‑RX Pro 2.4 GHz
- https://www.lrp.cc/fileadmin/product_downloads/instructions_en/87030.pdf
- Driver hat PCA9685
- https://www.nxp.com/docs/en/data-sheet/PCA9685.pdf


For the antenna receptor rc :

Driver hat **PC9685 is not designed to receive PWM signals.** It has registers that only delivers PWM.
So, the alternative is to connect (with Dupont wires ?) the ground with raspberry for each channel and
the PWM wire to a gpio on the raspberry and read it with c program, to call the existing functions
of the api (set_servo_motor, set_servo_angle). However, it is needed to verify the tension in order not to
destroy the pi (3.3V accepted). We could measure it with a multimeter.

### Library setup

```bash
sudo apt update
sudo apt install pigpio
```

Or for latest version

```bash
git clone https://github.com/joan2937/pigpio.git
cd pigpio
make
sudo make install
```

System-Wide

```bash
sudo systemctl enable pigpiod
sudo systemctl start pigpiod
```

## Library to control pwm RPI5 : libgpiod

**Works for Raspberry Pi 5!**

Kernelgit : https://git.kernel.org/pub/scm/libs/libgpiod/libgpiod.git/

Docs : https://libgpiod.readthedocs.io/en/latest/

### Requirements

Raspberry Pi 5 or any Linux board with GPIO support via *libgpiod*.
Build tools: *gcc*, *make*, *autoconf*, *automake*, *libtool*.
*pthread* library for multi-threading.

### Install

Latest version : 2.2.2. See in Kernelgit for latest.

```bash
cd ~
wget https://mirrors.edge.kernel.org/pub/software/libs/libgpiod/libgpiod-2.2.2.tar.xz
tar -xvf ./libgpiod-2.2.2.tar.xz
cd ./libgpiod-2.2.2/
./configure --enable-tools
make -j$(nproc)
sudo make install
sudo ldconfig
```

### Compile

```bash
gcc -std=c90 -Wall -Wextra -Wpedantic -Werror -O2 -o test_pwm main.c pwm_gpiod.c -lgpiod -lpthread
gcc -std=c90 -Wall -Wextra -Wpedantic -Werror -O2 -o test_pwm_perf main_perf.c pwm_gpiod_perf.c -lgpiod -lpthread
gcc -std=c90 -Wall -Wextra -Wpedantic -Werror -O2 -o test_pwm_perf main_perf.c reader_servo.c pwm_gpiod_perf.c utils.c -lgpiod -lpthread
```

### How it works

#### For dynamic-lines library

**pwm_gpiod**

Using libgpiod, we can make line requests to watch events associated to gpios.

The workflow is like this : 

→ GPIO chip
  → line request (request configuration) [config]
    → GPIO line (direction, edge detection, clock) [config, settings]
      → edge events (file descriptor, event buffer)

#### For performance library

**pwm_gpiod_perf**

Same as before but 1 line request that watch all predefined channels at initialization.