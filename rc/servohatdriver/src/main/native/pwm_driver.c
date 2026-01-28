#include "pwm_driver.h"
#include "utils.h"
#include "i2c.h"

#define PCA9685_ADDR     0x40 /*address of the hat servo driver*/
#define MODE1            0x00 /*register of config : cf page 14*/
#define PRESCALE         0xFE /*register of PWM : 11111110*/
#define LED0_ON_L        0x06 /*register of start/end PWM signal*/

static const char *TAG = "pwm_driver";

static int initialized = 0;

/*
=========================================================
DRIVER USEFUL FUNCTIONS
=========================================================
*/

/*set PWM frequency of driver*/
static int pca9685_set_pwm_freq(float freq) {
    float prescaleval;
    int prescale;
    int oldmode;
    int newmode;

    /*using prescale to generate the frequency we want on outputs
    based on the clock and pwm length
    formula from p25 of PCA9685 pdf*/
    prescaleval = PCA9685_CLOCK_FREQ; /*PCA9685 uses a 25MHz clock*/
    prescaleval /= (float)PCA9685_PWM_STEPS; /*every pwm cycle coded on 12 bits*/
    prescaleval /= freq; /*divide by freq wanted*/
    prescale = (int)(prescaleval + 0.5) - 1; /* replace round() for C90 */
    oldmode = i2c_read_byte(MODE1); /*read the current value of MODE1 reg*/
    if (oldmode < 0) {
        log_message(LOG_ERROR, TAG, "Error byte reading I2C of %02X", MODE1);
        return -1;
    }
    /*force the bit 7 to zero and the bit 4 to 1, do not change others (flags)
    7 -> restart mode, 4 -> sleep mode, cf page 14 PCA9685 pdf*/
    newmode = (oldmode & 0x7F) | MODE1_SLEEP; /*into sleep mode then*/
    /*the prescale value has to be modified in sleep mode cf page 14*/

    /*write the new mode into mode1 reg to set into sleep mode*/
    if (i2c_write_byte(MODE1, newmode) != 0) {
        return -1;
    }

    /*write the new prescale frequence into prescale reg*/
    if (i2c_write_byte(PRESCALE, prescale) != 0) {
        return -1;
    }

    /*return to the old mode*/
    if (i2c_write_byte(MODE1, oldmode) != 0) {
        return -1;
    }

    sleep_ms(5); /*wait for 5ms to set the value*/

    /*restart ok, pwm can be generated*/
    /*auto increment, force bit 7 to 1*/
    if (i2c_write_byte(MODE1, oldmode | MODE1_RESTART) != 0) {
        return -1;
    }

    log_message(LOG_DEBUG, TAG, "driver pwm frequency set for %.2f", freq);
    return 0;
}

/*Send PWM signal to driver*/
int pca9685_set_pwm(int channel, int on, int off) {

    if (!initialized) {
        log_message(LOG_ERROR, TAG, "Driver not initialized");
        return -1;
    }

    /*cast from int to hexa,
    each channel own 4 registers on low, on high, off low, 
    off high & 0xFF filters to have bits from 0 to 7 >> 8 : take bits from 11 to 8 (translation)*/

    if (i2c_write_byte(LED0_ON_L + 4 * channel, on & 0xFF) != 0) { /*on low*/
        return -1;
    }
    if (i2c_write_byte(LED0_ON_L + 4 * channel + 1, on >> 8) != 0) { /*on high*/
        return -1;
    }
    if (i2c_write_byte(LED0_ON_L + 4 * channel + 2, off & 0xFF) != 0) { /*off low*/
        return -1;
    }
    if (i2c_write_byte(LED0_ON_L + 4 * channel + 3, off >> 8) != 0) { /*off high*/
        return -1;
    }

    log_message(LOG_DEBUG, TAG,
        "driver pwm sent at channel %d, on: %d, off: %d",
        channel, on, off);

    return 0; /*all writes ok*/
}

/*
=========================================================
API FUNCTIONS
=========================================================
*/

int init_driver() {

    if (initialized) {
        return 0;
    }

    if (init_i2c() != 0) {
        return -1;
    }

    if (i2c_set_slave(PCA9685_ADDR) != 0) {
        return -1;
    }

    /*reset the module -> normal mode*/
    if (i2c_write_byte(MODE1, 0x00) != 0) {
        return -1;
    } 

    sleep_ms(1); /*sleep 1ms (stabilize the clock etc)*/

    /*50 Hz for servo as seen in set servo angle (20ms)*/
    if (pca9685_set_pwm_freq(DRIVER_FREQUENCY) != 0) {
        return -1;
    } 

    initialized = 1;

    log_message(LOG_INFO, TAG, "Driver initialized.");
    return 0;
}

/* Convert microseconds to PCA9685 12-bit duty */
int us_to_duty(float pulse_us) {
    float cycle_us; 
    int duty;
    cycle_us = 1000000.0f / (float)DRIVER_FREQUENCY;

    /*cross product; percentage of resolution*/
    duty = (int)(linear_reg_float(pulse_us, 0.0f, cycle_us, 0.0f, PCA9685_PWM_STEPS) + 0.5f);

    if (duty > PCA9685_PWM_STEPS - 1) duty = PCA9685_PWM_STEPS - 1;
    if (duty < 0) duty = 0;
    return duty;
}