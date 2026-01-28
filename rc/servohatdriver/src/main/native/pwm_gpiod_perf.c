#define _POSIX_C_SOURCE 199309L

#include "pwm_gpiod_perf.h"
#include "utils.h"
#include <time.h>
#include <gpiod.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>

#define EVENT_BUFFER_SIZE 16  /*max events to read*/
#define FAILSAFE_COUNT 20
#define FAILSAFE_TOL 10 /*failsafe tolerance in us*/

/*
This library uses one line request for all GPIOs lines.

It has better performance, but you cannot add lines or change them dynamically.
*/

static int initialized = 0;

/*struct representing one PWM line*/
typedef struct {
    int gpio; /*gpio num*/
    uint64_t rising_time;   /*in us, timestamp rising edge*/
    int pulse;              /*in us, pulse width*/
    pwm_callback_t callback; /*callback function on falling edge*/
    void *userdata; /*data to pass to callback*/
    int pulse_failsafe;
    int failsafe_count;
    int watchdog_time;
} PWMLineInternal;

/*struct representing lines*/
struct PWMReader {
    struct gpiod_chip *chip; /*chip : ex. gpiochip4*/
    PWMLineInternal **lines; /*array representing references to lines*/
    int num_lines; /*number of lines*/
    int capacity; /*max capacity (useful to realloc if needed)*/
    pthread_mutex_t lock; /*mutex for read/write*/
    pthread_t thread; /*thread*/
    struct gpiod_line_request *line_req; /*line request object*/
    int stop_thread; /*stop thread flag*/
    int *offset_to_idx;
};

/*event thread*/
static void *pwm_event_thread(void *arg)
{
    PWMReader *r;
    PWMReader *r2;
    struct gpiod_edge_event_buffer *buf;
    int nlines;
    int n;
    int j;
    PWMLineInternal *line;
    struct gpiod_edge_event *ev;
    int pulse;
    void *userdata;
    int gpio;
    uint64_t t;
    pwm_callback_t cb;
    int offset;

    r = (PWMReader*)arg;

    /*creates new event buffer with size*/
    buf = gpiod_edge_event_buffer_new(EVENT_BUFFER_SIZE);
    if (buf == NULL) {
        fprintf(stderr, "Failed to allocate edge event buffer\n");
        return NULL;
    }

    /*loop on flag*/
    while (!r->stop_thread) {

        /*read num lines safely*/
        pthread_mutex_lock(&r->lock);
        nlines = r->num_lines;
        pthread_mutex_unlock(&r->lock);

        /*if lines empty, wait a bit and skip*/
        if (nlines == 0) {
            sleep_ms(1000);
            continue;
        }

        /*read reader struct*/
        pthread_mutex_lock(&r->lock);
        r2 = r;
        pthread_mutex_unlock(&r->lock);

        /*read events from file descriptor fd*/
        n = gpiod_line_request_read_edge_events(r2->line_req, buf, EVENT_BUFFER_SIZE);
        if (n < 0) continue;

        for (j = 0; j < n; j++) {

            /*blocks until event*/
            ev = gpiod_edge_event_buffer_get_event(buf, j);
            pulse = 0;

            offset = gpiod_edge_event_get_line_offset(ev);
            offset = r2->offset_to_idx[offset];
            if (offset < 0) {
                continue;
            }
 
            pthread_mutex_lock(&r->lock);

            line = r->lines[offset];
            if (gpiod_edge_event_get_event_type(ev) == GPIOD_EDGE_EVENT_RISING_EDGE) {
                line->rising_time = gpiod_edge_event_get_timestamp_ns(ev) / 1000UL; /*us*/
                pthread_mutex_unlock(&r->lock);
            } else { /*falling edge*/
                t = gpiod_edge_event_get_timestamp_ns(ev) / 1000UL; /*us*/
                pulse = (int)(t - line->rising_time);
                line->pulse = pulse;

                if (line->pulse > line->pulse_failsafe - FAILSAFE_TOL &&
                    line->pulse < line->pulse_failsafe + FAILSAFE_TOL &&
                    line->pulse_failsafe >= 0) {
                        line->failsafe_count++;
                    } else {
                        line->failsafe_count = 0;
                    }

                /*copy data for callback*/
                gpio = line->gpio;
                userdata = line->userdata;
                cb = line->callback;
                pthread_mutex_unlock(&r->lock);

                if (cb) cb(gpio, pulse, userdata); /*callback function*/
                
            }
        }
    }

    /*free event buffer at the end*/
    gpiod_edge_event_buffer_free(buf);
    return NULL;
}

/*create a pwm, open chip by path (ex : gpiochip4)*/
/*see for "gpiodetect"*/
PWMReader *pwm_create(const char *chipname,
    const int pwm_count, const unsigned int *gpio_nums,
    pwm_callback_t *callbacks, void **userdatas,
    const int *failsafes, const int *watchdogs)
{
    PWMReader *r;
    struct gpiod_request_config *req_cfg;
    struct gpiod_line_settings *settings;
    struct gpiod_line_config *line_cfg;
    int i;

    if (initialized) {
        return NULL;
    } else {
        initialized = 1;
    }
    
    /*init base struct PWM Reader*/
    r = malloc(sizeof(PWMReader));
    if (!r) {
        initialized = 0;
        return NULL;
    }
    memset(r, 0, sizeof(PWMReader)); /*set all to 0*/

    /*open chip*/
    r->chip = gpiod_chip_open(chipname);
    if (!r->chip) {
        free(r);
        initialized = 0;
        return NULL;
    }

    /*init lines array in PWM Reader*/
    r->capacity = pwm_count;
    r->lines = malloc(sizeof(PWMLineInternal*) * r->capacity);
    if (!r->lines) {
        gpiod_chip_close(r->chip);
        free(r);
        initialized = 0;
        return NULL;
    }

    /*mapping offset - idx*/

    /*better with this, but gpio chip info have to be set*/
    /*for now, just a number*/
    /*size_lines = gpiod_chip_info_get_num_lines();*/
    r->offset_to_idx = malloc(sizeof(int) * 64);
    if (!r->offset_to_idx) {
        gpiod_chip_close(r->chip);
        free(r);
        initialized = 0;
        return NULL;
    }
    memset(r->offset_to_idx, -1, sizeof(int));

    /*init settings, req and line config*/
    settings = gpiod_line_settings_new();
    line_cfg = gpiod_line_config_new();
    req_cfg = gpiod_request_config_new();
    if (!settings || !line_cfg || !req_cfg) {
        gpiod_chip_close(r->chip);
        free(r);
        if (settings) gpiod_line_settings_free(settings);
        if (line_cfg) gpiod_line_config_free(line_cfg);
        if (req_cfg) gpiod_request_config_free(req_cfg);
        initialized = 0;
        return NULL;
    }

    /*set input direction*/
    gpiod_line_settings_set_direction(settings, GPIOD_LINE_DIRECTION_INPUT);
    /*set edge detection type*/
    gpiod_line_settings_set_edge_detection(settings, GPIOD_LINE_EDGE_BOTH);
    /*set bias : pull down or up value when disconnected*/
    /*down to avoid random pulses*/
    gpiod_line_settings_set_bias(settings, GPIOD_LINE_BIAS_PULL_DOWN);
    /*monotonic for more precise timestamps*/
    gpiod_line_settings_set_event_clock(settings, GPIOD_LINE_CLOCK_MONOTONIC);

    for (i = 0; i < pwm_count; i++) {

        /*init line struct*/
        r->lines[i] = malloc(sizeof(PWMLineInternal));
        if (!r->lines[i]) {
            /*free.. */
            initialized = 0;
            return NULL;
        }
        memset(r->lines[i], 0, sizeof(PWMLineInternal)); /*set all to 0*/

        /*init values*/
        r->lines[i]->gpio = gpio_nums[i];
        r->lines[i]->rising_time = 0;
        r->lines[i]->pulse = -1;
        r->lines[i]->callback = callbacks[i];
        r->lines[i]->userdata = userdatas[i];
        r->lines[i]->pulse_failsafe = failsafes[i];
        r->lines[i]->failsafe_count = 0;
        r->lines[i]->watchdog_time = watchdogs[i];
        r->offset_to_idx[gpio_nums[i]] = i;

        /*add line settings for a gpio*/
        gpiod_line_config_add_line_settings(line_cfg, &gpio_nums[i], 1, settings);

        /*free if error*/
    }

    /*request a line*/
    r->line_req = gpiod_chip_request_lines(r->chip, req_cfg, line_cfg);

    /*free configs, settings*/
    gpiod_request_config_free(req_cfg);
    gpiod_line_settings_free(settings);
    gpiod_line_config_free(line_cfg);

    /*init mutex and variables from PWM Reader*/
    r->num_lines = pwm_count;
    pthread_mutex_init(&r->lock, NULL);
    r->stop_thread = 0;

    /*create event thread*/
    if (pthread_create(&r->thread, NULL, pwm_event_thread, r) != 0) {
        free(r->lines);
        gpiod_chip_close(r->chip);
        pthread_mutex_destroy(&r->lock);
        free(r);
        initialized = 0;
        return NULL;
    }

    return r;
}

/*get current pulse of a gpio*/
int pwm_get_pulse(PWMReader *r, int gpio)
{
    int i;
    int pulse;

    if (!r) return -1;
    pthread_mutex_lock(&r->lock);

    /*search for the right gpio and return pulse*/
    i = r->offset_to_idx[gpio];
    if (i < 0) {
        pthread_mutex_unlock(&r->lock);
        return -1;
    }
    if (time_now_us() - r->lines[i]->rising_time > r->lines[i]->watchdog_time
        && r->lines[i]->watchdog_time >= 0) {
        pulse = -1;
    } else if (r->lines[i]->failsafe_count >= FAILSAFE_COUNT) {
        pulse = -1;
    } else {
        pulse = r->lines[i]->pulse;
    }
    pthread_mutex_unlock(&r->lock);
    return pulse;
}

/*close everything safely*/
void pwm_destroy(PWMReader *r)
{
    int i;

    if (!r) return;

    r->stop_thread = 1;
    pthread_join(r->thread, NULL);

    gpiod_line_request_release(r->line_req);
    for (i = 0; i < r->num_lines; i++) {
        free(r->lines[i]);
    }

    initialized = 0;
    gpiod_chip_close(r->chip);
    free(r->lines);
    free(r->offset_to_idx);
    pthread_mutex_destroy(&r->lock);
    free(r);
}
