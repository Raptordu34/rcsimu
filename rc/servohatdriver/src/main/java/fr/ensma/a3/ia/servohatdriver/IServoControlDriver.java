package fr.ensma.a3.ia.servohatdriver;

/**
 * Command driver API for controlling an ESC and a servo through I²C.
 *
 * <p>This interface provides all high-level functions used to control
 * acceleration, braking, reversing and steering, as well as reading
 * current PWM-related state values.</p>
 *
 * <h2>Overview</h2>
 * <p>
 * This API is designed to drive an Electronic Speed Controller (ESC) and a
 * servo motor via an I²C PWM driver board (PCA9685), typically used
 * for RC car control.  
 * Implementations of this interface must ensure correct initialization,
 * communication, and PWM handling.
 * </p>
 *
 * <h2>Reference Hardware</h2>
 * <ul>
 *     <li>ESC : LRP Variateur AI Runner Reverse V2.0 83020  
 *         <a href="https://www.lrp.cc/fileadmin/product_downloads/instructions_en/83020_en.pdf">
 *             https://www.lrp.cc/fileadmin/product_downloads/instructions_en/83020_en.pdf
 *         </a>
 *     </li>
 *
 *     <li>Servo motor : MG90S  
 *         <a href="https://dosya.motorobit.com/pdf/MG90S_Tower-Pro.pdf">
 *             https://dosya.motorobit.com/pdf/MG90S_Tower-Pro.pdf
 *         </a>
 *     </li>
 *
 *     <li>PWM driver : PCA9685  
 *         <a href="https://www.nxp.com/docs/en/data-sheet/PCA9685.pdf">
 *             https://www.nxp.com/docs/en/data-sheet/PCA9685.pdf
 *         </a>
 *     </li>
 * </ul>
 * @version 1.0
 *
 */
public interface IServoControlDriver {
    
    /**
     * Initializes the driver board.
     * <p>
     * Opens the I²C connection, selects the correct device address,
     * resets the PWM module and configures the operating frequency
     * (typically 50 Hz).  
     * The prescale value and mode registers are set according to
     * the PCA9685 documentation.
     * </p>
     */
    public Integer initDriver();

    /**
     * Closes the driver board.
     * <p>
     * Closes the existing I²C connection.
     * </p>
     */
    public void closeDriver();

    public Integer initGpioReading();

    public void closeGpioReading();

    public Integer readPwmPercentGpioMotor();

    public Integer readPwmPercentGpioServo();

    /**
     * Sends a command to the direction servo.
     * @param anglePercent value from -100 to 100
     * @return C function return code (0 = success, -1 = error)
     */
    public Integer setServoDutyDirection(final int anglePercent);

    /**
     * Sends a command to the motor ESC (Electronic Speed Controller).
     * @param speedPercent value from -100 to 100
     * @return C function return code (0 = success, -1 = error)
     */
    public Integer setMotorDutyEscLrp(final int speedPercent);

    /**
     * Sends a PWM signal to the camera horizontal axis servo.
     * @param pulseUs pulse width in microseconds
     * @return C function return code (0 = success, -1 = error)
     */
    public Integer setServoPulseCameraHor(final float pulseUs);

    /**
     * Sends a PWM signal to the camera vertical axis servo.
     * @param pulseUs pulse width in microseconds
     * @return C function return code (0 = success, -1 = error)
     */
    public Integer setServoPulseCameraVer(final float pulseUs);

    /**
     * Sends a manual command duty to the direction servo.
     * @param duty value from 0 to 4095
     * @return C function return code (0 = success, -1 = error)
     */
    public Integer setServoDutyDirectionManual(final int duty);

    /**
     * Sends a manual command duty to the motor ESC (Electronic Speed Controller).
     * @param duty value from 0 to 4095
     * @return C function return code (0 = success, -1 = error)
     */
    public Integer setMotorDutyEscLrpManual(final int duty);

}
