#ifndef PWM_GPIOD_PERF_H
#define PWM_GPIOD_PERF_H

#include <stdint.h>

typedef void (*pwm_callback_t)(int gpio, int pulse_us, void *userdata);

typedef struct PWMReader PWMReader;

PWMReader *pwm_create(const char *chipname,
    const int pwm_count, const unsigned int *gpio_nums,
    pwm_callback_t *callbacks, void **userdatas,
    const int *failsafes, const int *watchdogs);
int pwm_get_pulse(PWMReader *r, int gpio);
void pwm_destroy(PWMReader *r);

#endif
