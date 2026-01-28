package fr.ensma.a3.ia.servocontrolbusiness;

/**
 * 
 * @version 1.0
 * 
 * Interface to interact with the car implemented by {@link ServoControlBusinessAPI}
 * Create a new instance of {@link ServoControlBusinessAPI} and a {@link DriverData}
 * to start launching commands. 
 * 
 * upgrades : 
 * - data conversion in DriverData
 * - implements functions to read values from rc controller (in c api)
 * - separate the launchCommands function -> mode, direction, motor.
 * 
 */
public interface IServoControlBusinessAPI {

    /**
     * Close the driver. Don't forget to close the communication at the end of your program.
     */
    public void closeDriver();

    /**
     * This function launch all commands. 
     * It changes the mode, direction, motor.
     * Values are converted automatically. (maybe do it in DriverData at each setter? or is it done elsewhere)
     * @param driver driver from {@link DriverData}
     */
    public void launchCommands(final DriverData driver);

    /**
     * Function to see for specific motor pulses and adjust them correctly
     */
    public void sendManualMotorDuty(final Integer duty);

    /**
     * Function to see for specific servo pulses and adjust them correctly
     */
    public void sendManualServoDuty(final Integer duty);

    public void updateDriverByRc(DriverData driver);

    public Integer getMotorPercent();
    
    public Integer getDirectionPercent();

    public Integer getCurrentMode();
}
