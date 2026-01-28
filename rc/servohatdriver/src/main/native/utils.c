#define _POSIX_C_SOURCE 199309L   /* for usleep */

#include "utils.h"
#include <stdio.h>
#include <time.h>
#include <stdarg.h> /*for arg function in logger*/

static const char *TAG = "utils";

/*
=========================================================
USEFUL FUNCTIONS
=========================================================
*/

/*custom log function*/
void log_message(LogLevel level, const char* tag, const char *format, ...)
{
    FILE *f;
    time_t now;
    struct tm *tm_info;
    char buf[64];
    const char *level_str;

    if (level > LOG_LEVEL) {
        return;
    }

    /* log level */
    if (level == LOG_INFO) {
        level_str = "INFO";
    } else if (level == LOG_WARN) {
        level_str = "WARN";
    } else if (level == LOG_DEBUG) {
        level_str = "DEBUG";
    } else {
        level_str = "ERROR";
    }

    f = fopen("logs/log_driver_down_c.txt", "a"); /* open with append mode */
    if (f == NULL) {
        return;
    }

    /* Timestamp */
    now = time(NULL);
    tm_info = localtime(&now);
    strftime(buf, sizeof(buf), "%Y-%m-%d %H:%M:%S", tm_info);

    fprintf(f, "[%s] [%s] [%s]", buf, level_str, tag);

    /* format message */
    {
        va_list args;
        va_start(args, format);
        vfprintf(f, format, args);
        va_end(args);
    }

    fprintf(f, "\n");
    fclose(f);
}

/*POSIX's style sleep function*/
void sleep_ms(int milliseconds)
{
    struct timespec ts;
    ts.tv_sec = milliseconds / 1000;
    ts.tv_nsec = (milliseconds % 1000) * 1000000;
    log_message(LOG_DEBUG, TAG, "sleep for %d ms", milliseconds);
    nanosleep(&ts, NULL);
    log_message(LOG_DEBUG, TAG, "end of sleep for %d ms", milliseconds);
}

int clamp_int(int val, int min, int max) {
    if (val < min) return min;
    if (val > max) return max;
    return val;
}

float linear_reg_float(float x, float x_min, float x_max, float y_min, float y_max) {

    /*divide by 0*/
    if (x_max - x_min < 1e-3) {
        return y_min;
    }

    return y_min + (x - x_min) * (y_max - y_min) / (x_max - x_min);
}

uint64_t time_now_us()
{
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (uint64_t)ts.tv_sec * 1000000ULL + ts.tv_nsec / 1000ULL;
}