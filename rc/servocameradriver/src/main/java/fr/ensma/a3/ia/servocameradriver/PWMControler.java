package fr.ensma.a3.ia.servocameradriver;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class PWMControler {

    // PCA9685 Registers
    private static final int MODE1 = 0x00;
    // Debug flag
    private final boolean debug = false;
    // I2C Address
    private static final int ADDRESS = 0x40;
    private static final int PRESCALE = 0xFE;
    private static final int LED0_ON_L = 0x06;
    private final I2C i2c;
    // Singleton instance
    private static PWMControler INSTANCE;

    // Write a value to a register
    public void write(int reg, int value) {
        i2c.writeRegister(reg, (byte) (value & 0xFF));
        if (debug)
            System.out.printf("I2C: Wrote 0x%02X to 0x%02X%n", value, reg);
    }

    // Constructor for singleton pattern
    public PWMControler() {
        Context pi4j = Pi4J.newAutoContext();
        I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
        I2CConfig config = I2C.newConfigBuilder(pi4j)
                .bus(1)
                .device(ADDRESS)
                .id("PCA9685")
                .name("PWM Driver PCA9685")
                .build();
        this.i2c = i2CProvider.create(config);
        if (debug)
            System.out.println("Resetting PCA9685...");
        write(MODE1, 0x00);
    }

    // Constructor for singleton pattern with custom address
    public PWMControler(Context pi4j, int address) {
        I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
        I2CConfig config = I2C.newConfigBuilder(pi4j)
                .bus(1)
                .device(address)
                .id("PCA9685")
                .name("PWM Driver PCA9685")
                .build();
        this.i2c = i2CProvider.create(config);
        if (debug)
            System.out.println("Resetting PCA9685...");
        write(MODE1, 0x00);
    }

    // Get the singleton instance
    public static PWMControler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PWMControler();
        }
        return INSTANCE;
    }

    // Get the singleton instance with custom address
    public static PWMControler getInstance(Context pi4j, int address) {
        if (INSTANCE == null) {
            INSTANCE = new PWMControler(pi4j, address);
        }
        return INSTANCE;
    }

    // Read a value from a register
    public int read(int reg) {
        byte[] data = new byte[1];
        i2c.readRegister(reg, data, 0, 1);
        int result = data[0] & 0xFF;
        if (debug)
            System.out.printf("I2C: Read 0x%02X from 0x%02X%n", result, reg);
        return result;
    }

    // Set the PWM frequency
    public void setPWMFreq(double freq) {
        double prescaleVal = 25000000.0 / 4096.0 / freq - 1.0;
        int prescale = (int) Math.floor(prescaleVal + 0.5);
        if (debug)
            System.out.printf("Setting PWM freq: %.2f Hz, prescale=%d%n", freq, prescale);
        int oldMode = read(MODE1);
        int sleep = (oldMode & 0x7F) | 0x10; // sleep
        write(MODE1, sleep);
        write(PRESCALE, prescale);
        write(MODE1, oldMode);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        write(MODE1, oldMode | 0x80); // restart
    }

    // Set the pulse width for a specific channel
    public void setServoPulse(int channel, int pulseMicroseconds) {
        double pulse = pulseMicroseconds * 4096.0 / 20000.0; // 50 Hz
        int on = 0;
        int off = (int) pulse;
        int base = LED0_ON_L + 4 * channel;
        write(base, on & 0xFF);
        write(base + 1, on >> 8);
        write(base + 2, off & 0xFF);
        write(base + 3, off >> 8);
        if (debug)
            System.out.printf("Channel %d -> ON: %d OFF: %d%n", channel, on, off);

    }
}
