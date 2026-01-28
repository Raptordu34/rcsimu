#include "pwm_reader.h"
#include "utils.h"
#include <pigpio.h>
#include <stdlib.h>
#include <pthread.h>


#define MAX_PWM_READERS 4

#define TIMEOUT_WATCHDOG 100

static const char* TAG = "pwm_reader";

typedef struct {
    int gpio_pin;
    int high_tick;
    int pulse_width;
} pwm_reader_t;

static pwm_reader_t readers[MAX_PWM_READERS];

static pthread_mutex_t readers_mutex;

static int initialized = 0;

/**
 * Callback called by pigpio when the GPIO level changes
 */
static void pwm_callback(int gpio, int level, uint32_t tick) {
    int i;
    int tmp;
    pwm_reader_t *r;

    if (!initialized) {
        log_message(LOG_ERROR, TAG, "PWM reader not initialized");
        return;
    }

    if (pthread_mutex_lock(&readers_mutex) != 0) {
        log_message(LOG_ERROR, TAG, "Failed to take mutex");
        return;
    }

    for (i=0; i<MAX_PWM_READERS; i++) {
        r = &readers[i];
        if (r->gpio_pin != gpio) continue; /*go next index if not gpio*/

        if(level == 1) {
            r->high_tick = tick;
        } else if(level == 0) {
            tmp = tick - r->high_tick;

            /*+- 5 tolerance*/
            if(r->pulse_width < 0 || abs(r->pulse_width - tmp) > 6) {
                r->pulse_width = tmp;
                log_message(LOG_DEBUG, TAG, "Pulse width of index %d set to %d",
                    r->gpio_pin, r->pulse_width);
            }

        } else if(level == PI_TIMEOUT) {
            r->pulse_width = -1;
            log_message(LOG_WARN, TAG, "PWM timeout on GPIO %d", gpio);
        }
    }

    if (pthread_mutex_unlock(&readers_mutex) != 0) {
        log_message(LOG_ERROR, TAG, "Failed to release mutex");
    }
}

/**
 * Initialize GPIO for PWM reading
 */
int pwm_init_reader(int idx, int gpio_pin) {

    int i;

    if(idx < 0 || idx >= MAX_PWM_READERS) {
        log_message(LOG_ERROR, TAG, "Index %d out of bounds", idx);
        return -1;
    }

    if(!initialized) {

        if (pthread_mutex_init(&readers_mutex, NULL) != 0) {
            log_message(LOG_ERROR, TAG, "Failed to initialize mutex");
            return -1;
        }

        for (i = 0; i < MAX_PWM_READERS; i++) {
            readers[i].gpio_pin = -1;
            readers[i].pulse_width = -1;
            readers[i].high_tick = 0;
        }

        if(gpioInitialise() < 0) {
            log_message(LOG_ERROR, TAG, "Fail to initialize GPIO");
            return -1; /* fail */
        }

        initialized = 1;
        log_message(LOG_INFO, TAG, "GPIO Initialized");
    }

    if (readers[idx].gpio_pin != -1) {
        log_message(LOG_WARN, TAG, "Index %d already initialized for reading", idx);
        return -1;
    }

    if (gpioSetMode(gpio_pin, PI_INPUT) != 0) {
        log_message(LOG_ERROR, TAG, "Fail to set input mode for pin %d", gpio_pin);
        return -1;
    }

    if (gpioSetAlertFunc(gpio_pin, pwm_callback) != 0) {
        log_message(LOG_ERROR, TAG, "Fail to set callback for pin %d", gpio_pin);
        return -1;
    }

    if (gpioSetWatchdog(gpio_pin, TIMEOUT_WATCHDOG) != 0) {
        log_message(LOG_ERROR, TAG, "Fail to set watchdog for pin %d", gpio_pin);
        return -1;
    }

    readers[idx].gpio_pin = gpio_pin;

    log_message(LOG_INFO, TAG, "GPIO reading initialized for pin %d", gpio_pin);
    return 0;
}

/**
 * Read PWM pulse width in microseconds
 */
int pwm_read_us(int idx) {
    int val;

    if (!initialized) {
        log_message(LOG_ERROR, TAG, "PWM reading not initialized");
        return -1;
    }

    if(idx < 0 || idx >= MAX_PWM_READERS) {
        log_message(LOG_ERROR, TAG, "Index out of bounds");
        return -1;
    }

    if (pthread_mutex_lock(&readers_mutex) != 0) {
        log_message(LOG_ERROR, TAG, "Failed to take mutex");
        return -1;
    }

    if (readers[idx].gpio_pin == -1) {
        log_message(LOG_ERROR, TAG, "GPIO not initialized at index %d", idx);

        if (pthread_mutex_unlock(&readers_mutex) != 0) {
            log_message(LOG_ERROR, TAG, "Failed to release mutex");
        }

        return -1;
    }
    val = readers[idx].pulse_width;
    log_message(LOG_DEBUG, TAG, "Pulse red : %d, at index %d", val, idx);
    
    if (pthread_mutex_unlock(&readers_mutex) != 0) {
        log_message(LOG_ERROR, TAG, "Failed to release mutex");
        return -1;
    }

    return val;
}

/**
 * Stop monitoring the GPIO and free resources
 */
void pwm_close_all() {

    if (pthread_mutex_destroy(&readers_mutex) != 0) {
        log_message(LOG_ERROR, TAG, "Failed to destroy mutex");
    }

    gpioTerminate();
    initialized = 0;

    log_message(LOG_INFO, TAG, "PWM reading closed");
}

