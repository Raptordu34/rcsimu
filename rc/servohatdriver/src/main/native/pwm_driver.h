#ifndef PWM_DRIVER_H_
#define PWM_DRIVER_H_

#define DRIVER_FREQUENCY 50
#define PCA9685_CLOCK_FREQ 25000000UL   /* 25 MHz internal clock */
#define PCA9685_PWM_STEPS  4096        /* 12-bit PWM */
#define MODE1_RESTART      0x80
#define MODE1_SLEEP        0x10

/**
 * @mainpage Command driver API
 *
 * Welcome to the Command driver documentation.
 *
 * @section overview Overview
 * This project controls an ESC and servo using I²C and a gamepad.
 *
 * @section modules_link Functions
 * To see all functions provided by this API : \ref modules "Functions".
 *
 * @section references References
 * See \ref references_page "References" for hardware datasheets and registers.
 */

 /** @page modules Modules
 *  This section contains all the function groups of the Command driver API.
 *
 *  - {@ref commands}
 *  - {@ref getters}
 */

/**
 * @page references_page References
 *
 * - ESC : LRP Variateur AI Runner Reverse V2.0 83020 
 *   https://www.lrp.cc/fileadmin/product_downloads/instructions_en/83020_en.pdf
 * - Servo motor : MG90S  
 *   https://dosya.motorobit.com/pdf/MG90S_Tower-Pro.pdf
 * - PCA9685 manual 
 *   https://www.nxp.com/docs/en/data-sheet/PCA9685.pdf
 *   registers can be found in the documentation of the servo driver
 */

/** @defgroup commands Commands
 *  Functions to control the motor and servo.
 *  @{
 */

/**
 * @brief Initializes the driver hat.
 * @details
 * Opens the I²C connection by opening the right linux file,
 * then set the driver address into this file,
 * reset the module and set the right PWM frequence
 * according to the clock (50Hz) and a formula using a
 * prescale value that can be found on the driver's manual.
 * The mode of the module is also changed by registers in order
 * to write the frequence.
 * 
 */
int init_driver();

int pca9685_set_pwm(int channel, int on, int off);

int us_to_duty(float pulse_us);

/** @} */

#endif /* PWM_DRIVER_H_ */
