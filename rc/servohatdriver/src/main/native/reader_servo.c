#include "reader_servo.h"
#include "utils.h"
#if USE_PIGPIO
#include "pwm_reader.h"
#else
#include "pwm_gpiod_perf.h"
#endif

#include <stdio.h>
#include <unistd.h>

#define MOTOR_GPIO_PIN 24
#define SERVO_GPIO_PIN 23

#define MOTOR_IDX 0
#define SERVO_IDX 1

#define USE_PATATOR_RC 0

#if USE_PATATOR_RC
#define MIN_PWM_SERVO 1005
#define MAX_PWM_SERVO 2030
#define MIN_PWM_MOTOR 965
#define MAX_PWM_MOTOR 2005
#else
#define MIN_PWM_SERVO 985
#define MAX_PWM_SERVO 2180
#define MIN_PWM_MOTOR 955
#define MAX_PWM_MOTOR 2120
#endif

static const char* TAG = "reader_servo";

#if !USE_PIGPIO
static PWMReader *r;
#endif

static const int gpio_idx[] = {
    MOTOR_GPIO_PIN,
    SERVO_GPIO_PIN
};

/**
 * Read PWM pulse as percentage
 */
static int pwm_read_percent(int idx) {
    int width;
    int val;
    
    #if USE_PIGPIO
    width = pwm_read_us(idx);
    #else
    width = pwm_get_pulse(r, gpio_idx[idx]);
    #endif
    if (width < 0) {
        return -101;
    }

    switch (idx)
    {
    case MOTOR_IDX:
        width = clamp_int(width, MIN_PWM_MOTOR, MAX_PWM_MOTOR);
        val = (int)(linear_reg_float(width,
            MIN_PWM_MOTOR, MAX_PWM_MOTOR, -100.0f, 100.0f) - 0.5f);
        break;
    
    case SERVO_IDX:
        width = clamp_int(width, MIN_PWM_SERVO, MAX_PWM_SERVO);
        val = (int)(linear_reg_float(width,
            MIN_PWM_SERVO, MAX_PWM_SERVO, -100.0f, 100.0f) - 0.5f);
        break;
    
    default:
        val = -101;
        break;
    }

    log_message(LOG_INFO, TAG, "Percent red : %d, at index %d", val, idx);

    return val;
}

#if USE_PIGPIO

int pwm_init_reader_servos() {
    if (pwm_init_reader(MOTOR_IDX, MOTOR_GPIO_PIN) != 0) {
        return -1;
    }
    if (pwm_init_reader(SERVO_IDX, SERVO_GPIO_PIN) != 0) {
        return -1;
    }
    return 0;
}

#else

static void callback_function(int gpio, int pulse_us, void *userdata) {
    (void)userdata;
    log_message(LOG_DEBUG, TAG, "GPIO %d pulse = %d us\n", gpio, pulse_us);
}

int pwm_init_reader_servos() {

    /* GPIOs list to read */
    unsigned int gpios[] = {MOTOR_GPIO_PIN, SERVO_GPIO_PIN};
    pwm_callback_t callbacks[] = {callback_function, callback_function};
    void *userdatas[] = {NULL, NULL};
    int failsafes[] = {1530, 1530};
    int watchdogs[] = {200000, 200000};

    /*
    gpiodetect -> see the "gpiochip" to put
    gpioinfo gpiochip4 -> to see offset / gpio. to enter : offset!
    */


    /* Create PWMREADER */
    r = pwm_create("/dev/gpiochip4",
                            2,         /* number of GPIOs */
                            gpios,
                            callbacks,
                            userdatas,
                            failsafes,
                            watchdogs);
    
    if (!r) {
        log_message(LOG_ERROR, TAG, "error creating pwm reader");
        return -1;
    }

    return 0;
}

int pwm_read_percent_motor() {
    return pwm_read_percent(MOTOR_IDX);
}

int pwm_read_percent_servo() {
    return pwm_read_percent(SERVO_IDX);
}

void pwm_close_all() {
    pwm_destroy(r);
}

#endif
