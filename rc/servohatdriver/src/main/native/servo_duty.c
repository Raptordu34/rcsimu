#include "servo_duty.h"
#include "pwm_driver.h"
#include "utils.h"

static const char *TAG = "servo_duty";

#define SERVO_DIRECTION_CHANNEL 0
#define SERVO_DIRECTION_MIN_DUTY 250
#define SERVO_DIRECTION_MAX_DUTY 375

#define MOTOR_ESC_CHANNEL 1
#define MOTOR_ESC_MIN_DUTY 140 /*not fully charged*/
#define MOTOR_ESC_MID_DUTY 400 /*not fully charged*/
#define MOTOR_ESC_MAX_DUTY 535 /*not fully charged*/

#define SERVO_CAMERA_HORIZ_CHANNEL 3

#define SERVO_CAMERA_VERT_CHANNEL 2

#define CONFIG_TEST 0

/*
=========================================================
COMMANDS FUNCTIONS
=========================================================
*/

/**
 * Send a servo command (0° to 180°)
 * 
 * Mapping angle_percent → PWM pulse_angle
 * ---------------------------------------
 * | speed_percent | PWM (pulse_angle)     |
 * |---------------|-----------------------|
 * | +100%         |    440  (180°)        |
 * | 50%           |    345  (90°)         |
 * | 0%            |    250  (0°)          |
 *
 */
int set_servo_duty_direction(int angle_percent) {
    int duty;

    duty = (int)linear_reg_float(angle_percent,
        -100.0f, 100.0f,
        SERVO_DIRECTION_MIN_DUTY, SERVO_DIRECTION_MAX_DUTY
    );

#if !CONFIG_TEST
    /*set the pwm value*/
    if (pca9685_set_pwm(SERVO_DIRECTION_CHANNEL, 0, duty) != 0) {
        return -1;
    }
#endif

    log_message(LOG_INFO, TAG, "driver servo direction pwm %d, sent at channel : %d",
        SERVO_DIRECTION_CHANNEL, duty);
    return 0;
}

/**
 * Send a motor variator command (forward/reverse)
 * 
 * Mapping speed_percent → PWM pulse_motor
 * ---------------------------------------
 * | speed_percent | PWM (pulse_motor)     |
 * |---------------|-----------------------|
 * | +100%         | 155  (full forward)   |
 * | 0%            | 420  (stop)           |
 * | -100%         | 600  (full backward)  |
 *
 * Example :
 *   forward 50%  → pulse_motor = 420 - (420-155)*50/100 = 288
 *   backward 50% → pulse_motor = 420 - (600-420)*(-50)/100 = 510
 *
 */
int set_motor_duty_esc_lrp(int speed_percent) {
    int duty;

    if(speed_percent > 0) {          /*forward*/
        duty = (int)linear_reg_float(speed_percent,
            0.0f, 100.0f,
            MOTOR_ESC_MID_DUTY, MOTOR_ESC_MAX_DUTY
        );
    } else if(speed_percent < 0) {   /*backward*/
        duty = (int)linear_reg_float(speed_percent,
            -100.0f, 0.0f,
            MOTOR_ESC_MIN_DUTY, MOTOR_ESC_MID_DUTY
        );
    } else {                         /*stop*/
        duty = MOTOR_ESC_MID_DUTY;
    }

#if !CONFIG_TEST
    /*set the pwm value*/
    if (pca9685_set_pwm(MOTOR_ESC_CHANNEL, 0, duty) != 0) {
        return -1;
    }
#endif

    log_message(LOG_INFO, TAG, "driver ESC pwm %d, sent at channel : %d",
        MOTOR_ESC_CHANNEL, duty);
    return 0;
}

/**
 * Send a servo command (0° to 180°)
 * 
 * Mapping angle_percent → PWM pulse_angle
 * ---------------------------------------
 * | speed_percent | PWM (pulse_angle)     |
 * |---------------|-----------------------|
 * | +100%         |    440  (180°)        |
 * | 50%           |    345  (90°)         |
 * | 0%            |    250  (0°)          |
 *
 */
int set_servo_pulse_camera_hor(float pulse_us) {
    int duty;

    duty = us_to_duty(pulse_us);
    
#if !CONFIG_TEST
    /*set the pwm value*/
    if (pca9685_set_pwm(SERVO_CAMERA_HORIZ_CHANNEL, 0, duty) != 0) {
        return -1;
    }
#endif

    log_message(LOG_INFO, TAG, "driver servo camera horizontal pwm %d, sent at channel : %d",
        SERVO_CAMERA_HORIZ_CHANNEL, duty);
    return 0;
}

/**
 * Send a servo command (0° to 180°)
 * 
 * Mapping angle_percent → PWM pulse_angle
 * ---------------------------------------
 * | speed_percent | PWM (pulse_angle)     |
 * |---------------|-----------------------|
 * | +100%         |    440  (180°)        |
 * | 50%           |    345  (90°)         |
 * | 0%            |    250  (0°)          |
 *
 */
int set_servo_pulse_camera_ver(float pulse_us) {
    int duty;

    duty = us_to_duty(pulse_us);

#if !CONFIG_TEST
    /*set the pwm value*/
    if (pca9685_set_pwm(SERVO_CAMERA_VERT_CHANNEL, 0, duty) != 0) {
        return -1;
    }
#endif

    log_message(LOG_INFO, TAG, "driver servo camera vertical pwm %d, sent at channel : %d",
        SERVO_CAMERA_VERT_CHANNEL, duty);
    return 0;
}

int set_servo_duty_direction_manual(int duty) {

#if !CONFIG_TEST
    /*set the pwm value*/
    if (pca9685_set_pwm(SERVO_DIRECTION_CHANNEL, 0, duty) != 0) {
        return -1;
    }
#endif

    log_message(LOG_INFO, TAG, "driver manual servo direction pwm %d, sent at channel : %d",
        SERVO_DIRECTION_CHANNEL, duty);
    return 0;
}

int set_motor_duty_esc_lrp_manual(int duty) {

#if !CONFIG_TEST
    /*set the pwm value*/
    if (pca9685_set_pwm(MOTOR_ESC_CHANNEL, 0, duty) != 0) {
        return -1;
    }
#endif

    log_message(LOG_INFO, TAG, "driver manual ESC pwm %d, sent at channel : %d",
        MOTOR_ESC_CHANNEL, duty);
    return 0;
}