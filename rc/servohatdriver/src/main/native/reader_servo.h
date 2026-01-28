#ifndef READER_SERVO_H_
#define READER_SERVO_H_

#define USE_PIGPIO 0 /*not available on pi5*/

int pwm_read_percent_servo();

int pwm_read_percent_motor();

int pwm_init_reader_servos();

/**
 * Free resources and stop monitoring the GPIO pin.
 * @param gpio_pin GPIO number
 */
void pwm_close_all();

#endif
