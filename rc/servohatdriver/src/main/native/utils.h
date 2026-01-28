#ifndef UTILS_H_
#define UTILS_H_

#define LOG_LEVEL LOG_INFO

#include <stdint.h>

 /*enum for log levels*/
 typedef enum {
    LOG_INFO = 2,
    LOG_WARN = 1,
    LOG_ERROR = 0,
    LOG_DEBUG = 3
} LogLevel;

void log_message(LogLevel level, const char* TAG, const char *format, ...);
void sleep_ms(int milliseconds);
int clamp_int(int val, int min, int max);
float linear_reg_float(float x, float x_min, float x_max, float y_min, float y_max);
uint64_t time_now_us();

#endif /* UTILS_H_ */
