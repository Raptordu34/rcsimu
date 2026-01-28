package fr.ensma.a3.ia.servohatdriver;

import java.nio.file.Path;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * This class is binding all all functions from C-API into a Java API, using Panama
 * 
 * @version 1.0
 */
public class ServoControlDriver implements IServoControlDriver{

    private static volatile ServoControlDriver refSingle; //multi-thread

    private static MethodHandle mhInit;
    private static MethodHandle mhClose;

    private static MethodHandle mhInitPwmReader;
    private static MethodHandle mhClosePwmReader;
    private static MethodHandle mhReadPercentMotor;
    private static MethodHandle mhReadPercentServo;

    private static MethodHandle mhSetServoDutyDirection;
    private static MethodHandle mhSetMotorDutyEscLrp;
    private static MethodHandle mhSetServoPulseCameraHor;
    private static MethodHandle mhSetServoPulseCameraVer;

    private static MethodHandle mhSetServoDutyDirectionManual;
    private static MethodHandle mhSetMotorDutyEscLrpManual;
    
    private final Arena arena;
    private final SymbolLookup lookup;

    private ServoControlDriver () {

        //todo : variable environnement export.. et getenv
        String libPathStr = new java.io.File("target/native/libdriver.so").getAbsolutePath();
        System.load(libPathStr);

        Linker linker = Linker.nativeLinker();

        // single thread only. use ofShared() for multiple threads, but handle parallelism.
        arena = Arena.ofConfined();

        lookup = SymbolLookup.libraryLookup(Path.of(libPathStr), arena);

        mhInit = linker.downcallHandle(
            lookup.find("init_driver").orElseThrow(() ->
                    new RuntimeException("Symbol 'init_driver' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT)
        );

        mhInitPwmReader = linker.downcallHandle(
            lookup.find("pwm_init_reader_servos").orElseThrow(() ->
                    new RuntimeException("Symbol 'pwm_init_reader_servos' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT)
        );

        mhClosePwmReader = linker.downcallHandle(
            lookup.find("pwm_close_all").orElseThrow(() ->
                    new RuntimeException("Symbol 'pwm_close_all' not found in libdriver.so")),
            FunctionDescriptor.ofVoid()
        );

                mhReadPercentMotor = linker.downcallHandle(
            lookup.find("pwm_read_percent_motor").orElseThrow(() ->
                    new RuntimeException("Symbol 'pwm_read_percent_motor' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT)
        );

        mhReadPercentServo = linker.downcallHandle(
            lookup.find("pwm_read_percent_servo").orElseThrow(() ->
                    new RuntimeException("Symbol 'pwm_read_percent_servo' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT)
        );

        mhClose = linker.downcallHandle(
            lookup.find("close_i2c").orElseThrow(() ->
                    new RuntimeException("Symbol 'close_i2c' not found in libdriver.so")),
            FunctionDescriptor.ofVoid()
        );

        // Servo direction (angle_percent)
        mhSetServoDutyDirection = linker.downcallHandle(
            lookup.find("set_servo_duty_direction").orElseThrow(() ->
                new RuntimeException("Symbol 'set_servo_duty_direction' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );

        // Motor ESC (speed_percent)
        mhSetMotorDutyEscLrp = linker.downcallHandle(
            lookup.find("set_motor_duty_esc_lrp").orElseThrow(() ->
                new RuntimeException("Symbol 'set_motor_duty_esc_lrp' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );

        // Servo camera horizontal (pulse_us)
        mhSetServoPulseCameraHor = linker.downcallHandle(
            lookup.find("set_servo_pulse_camera_hor").orElseThrow(() ->
                new RuntimeException("Symbol 'set_servo_pulse_camera_hor' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_FLOAT)
        );

        // Servo camera vertical (pulse_us)
        mhSetServoPulseCameraVer = linker.downcallHandle(
            lookup.find("set_servo_pulse_camera_ver").orElseThrow(() ->
                new RuntimeException("Symbol 'set_servo_pulse_camera_ver' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_FLOAT)
        );

        // Servo direction (angle_percent)
        mhSetServoDutyDirectionManual = linker.downcallHandle(
            lookup.find("set_servo_duty_direction_manual").orElseThrow(() ->
                new RuntimeException("Symbol 'set_servo_duty_direction_manual' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );

        // Motor ESC (speed_percent)
        mhSetMotorDutyEscLrpManual = linker.downcallHandle(
            lookup.find("set_motor_duty_esc_lrp_manual").orElseThrow(() ->
                new RuntimeException("Symbol 'set_motor_duty_esc_lrp_manual' not found in libdriver.so")),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );

        logger.debug("Method handlers intiliazed");
        
    }

    public static ServoControlDriver getSingleRef() {
        if (refSingle == null) {
            //mutex
            synchronized (ServoControlDriver.class) {
                if (refSingle == null) {
                    refSingle = new ServoControlDriver();
                    logger.info("Panama initialized");
                }
            }
        }
        return refSingle;
    }

    @Override
    public Integer initDriver() {
        try {
            logger.debug("Invoke init driver function");
            return (int) mhInit.invoke();
        } catch (Throwable e) { 
            logger.error("Unable to invoke init driver", e);
        }
        return -1;
    }

    @Override
    public void closeDriver() {
        try {
            logger.debug("Invoke close driver function");
            mhClose.invoke();
        } catch (Throwable e) { 
            logger.error("Unable to invoke close driver", e);
        }
    }

    @Override
    public Integer readPwmPercentGpioMotor() {
        try {
            logger.debug("Invoke read percent pwm motor");
            return (int) mhReadPercentMotor.invoke();
        } catch (Throwable e) { 
            logger.error("Unable to invoke read percent pwm motor", e);
        }
        return null;
    }

    @Override
    public void closeGpioReading() {
        try {
            logger.debug("Invoke close pwm reading function");
            mhClosePwmReader.invoke();
        } catch (Throwable e) { 
            logger.error("Unable to invoke close pwm reading", e);
        }
    }

    @Override
    public Integer initGpioReading() {
        try {
            logger.debug("Invoke init pvm reader");
            return (int) mhInitPwmReader.invoke();
        } catch (Throwable e) { 
            logger.error("Unable to invoke init pvm reader", e);
        }
        return null;
    }

    @Override
    public Integer readPwmPercentGpioServo() {
        try {
            logger.debug("Invoke read percent pwm servo");
            return (int) mhReadPercentServo.invoke();
        } catch (Throwable e) { 
            logger.error("Unable to invoke read percent pwm servo", e);
        }
        return null;
    }

    @Override
    public Integer setServoDutyDirection(final int anglePercent) {
        try {
            return (int) mhSetServoDutyDirection.invoke(anglePercent);
        } catch (Throwable e) {
            logger.error("Unable to invoke setServoDutyDirection", e);
        }
        return -1;
    }

    @Override
    public Integer setMotorDutyEscLrp(final int speedPercent) {
        try {
            return (int) mhSetMotorDutyEscLrp.invoke(speedPercent);
        } catch (Throwable e) {
            logger.error("Unable to invoke setMotorDutyEscLrp", e);
        }
        return -1;
    }

    @Override
    public Integer setServoPulseCameraHor(final float pulseUs) {
        try {
            return (int) mhSetServoPulseCameraHor.invoke(pulseUs);
        } catch (Throwable e) {
            logger.error("Unable to invoke setServoPulseCameraHor", e);
        }
        return -1;
    }

    @Override
    public Integer setServoPulseCameraVer(final float pulseUs) {
        try {
            return (int) mhSetServoPulseCameraVer.invoke(pulseUs);
        } catch (Throwable e) {
            logger.error("Unable to invoke setServoPulseCameraVer", e);
        }
        return -1;
    }

    @Override
    public Integer setServoDutyDirectionManual(final int duty) {
        try {
            return (int) mhSetServoDutyDirectionManual.invoke(duty);
        } catch (Throwable e) {
            logger.error("Unable to invoke setServoDutyDirectionManual", e);
        }
        return -1;
    }

    @Override
    public Integer setMotorDutyEscLrpManual(final int duty) {
        try {
            return (int) mhSetMotorDutyEscLrpManual.invoke(duty);
        } catch (Throwable e) {
            logger.error("Unable to invoke setMotorDutyEscLrpManual", e);
        }
        return -1;
    }

    private static final Logger logger = LogManager.getLogger(ServoControlDriver.class);
}

