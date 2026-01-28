#ifndef PWM_READER_H
#define PWM_READER_H

/**
 * Read the last pulse width in microseconds.
 * @param gpio_pin GPIO number
 * @return pulse width in microseconds, or -1 if error
 */
int pwm_read_us(int idx);

/**
 * Free resources and stop monitoring the GPIO pin.
 * @param gpio_pin GPIO number
 */
void pwm_close_all();

int pwm_init_reader(int idx, int gpio_pin);

#endif
