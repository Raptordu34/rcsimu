#include "i2c.h"
#include "utils.h"
#include <stdio.h>
#include <unistd.h>        /* for read et write functions */
#include <fcntl.h>         /* for O_RDWR */
#include <linux/i2c-dev.h> /* for I2C_SLAVE */
#include <sys/ioctl.h>     /* for ioctl */
#include <pthread.h>

static int i2c_ref;
static const char *device = "/dev/i2c-1"; /* path for the I²C */
static const char *TAG = "I2C";

static int initialized = 0;
static pthread_mutex_t i2c_mutex = PTHREAD_MUTEX_INITIALIZER;

/*
=========================================================
HELPER FUNCTIONS
=========================================================
*/

/* Unlock mutex and log error if unlocking fails */
static int unlock_mutex_error(pthread_mutex_t *mutex, const char *tag)
{
    int ret;

    ret = pthread_mutex_unlock(mutex);
    if (ret != 0)
    {
        log_message(LOG_ERROR, tag, "Failed to release mutex");
    }
    return ret;
}

/*
=========================================================
I²C USEFUL FUNCTIONS
=========================================================
*/

/* write byte by I²C */
int i2c_write_byte(int reg, int value)
{
    int ret;
    unsigned char buffer[2]; /* char from 0 to 255, like registers */

    /* lock mutex to make thread-safe */
    if (pthread_mutex_lock(&i2c_mutex) != 0)
    {
        log_message(LOG_ERROR, TAG, "Failed to take mutex");
        return -1;
    }

    /* check initialization */
    if (!initialized)
    {
        log_message(LOG_ERROR, TAG, "I2C not initialized");
        unlock_mutex_error(&i2c_mutex, TAG);
        return -1;
    }

    buffer[0] = (unsigned char)reg;   /* implicit cast from int to unsigned char */
    buffer[1] = (unsigned char)value; /* implicit cast from int to unsigned char */

    /* send data: using linux function write, returns number of bytes written */
    ret = write(i2c_ref, buffer, 2);
    if (ret != 2)
    {
        log_message(LOG_ERROR, TAG, "failed to write byte at register %d", reg);
        unlock_mutex_error(&i2c_mutex, TAG);
        return -1;
    }

    log_message(LOG_DEBUG, TAG, "byte %d written at %d register.", value, reg);

    /* unlock mutex */
    if (unlock_mutex_error(&i2c_mutex, TAG) != 0) {
        return -1;
    }

    return 0;
}

/* read byte by I²C */
int i2c_read_byte(int reg)
{
    int ret;
    unsigned char buffer[1]; /* char from 0 to 255, like registers */
    int value;

    /* lock mutex to make thread-safe */
    if (pthread_mutex_lock(&i2c_mutex) != 0)
    {
        log_message(LOG_ERROR, TAG, "Failed to take mutex");
        return -1;
    }

    /* check initialization */
    if (!initialized)
    {
        log_message(LOG_ERROR, TAG, "I2C not initialized");
        unlock_mutex_error(&i2c_mutex, TAG);
        return -1;
    }

    buffer[0] = (unsigned char)reg; /* implicit cast from int to unsigned char */

    /* write the address of the register */
    ret = write(i2c_ref, buffer, 1);
    if (ret != 1)
    {
        log_message(LOG_ERROR, TAG, "failed to write byte at register %d", reg);
        unlock_mutex_error(&i2c_mutex, TAG);
        return -1;
    }

    /* read the value of the register */
    ret = read(i2c_ref, buffer, 1);
    if (ret != 1)
    {
        log_message(LOG_ERROR, TAG, "failed to read byte at register %d", reg);
        unlock_mutex_error(&i2c_mutex, TAG);
        return -1;
    }

    value = (int)buffer[0]; /* implicit cast from unsigned char to int */

    log_message(LOG_DEBUG, TAG, "byte read at register %d, value: %d", reg, value);

    /* unlock mutex */
    if (unlock_mutex_error(&i2c_mutex, TAG) != 0) {
        return -1;
    }

    return value;
}

/* open the connection with I2C with read/write */
int init_i2c(void)
{

    /* lock mutex */
    if (pthread_mutex_lock(&i2c_mutex) != 0)
    {
        log_message(LOG_ERROR, TAG, "Failed to take mutex");
        return -1;
    }

    if (initialized)
    {
        log_message(LOG_INFO, TAG, "I2C already initialized");
        unlock_mutex_error(&i2c_mutex, TAG);
        return 0;
    }

    i2c_ref = open(device, O_RDWR);
    if (i2c_ref < 0)
    {
        log_message(LOG_ERROR, TAG, "failed to open the I²C connection");
        unlock_mutex_error(&i2c_mutex, TAG);
        return -1;
    }

    initialized = 1;
    log_message(LOG_INFO, TAG, "I2C initialized");

    /* unlock mutex */
    if (unlock_mutex_error(&i2c_mutex, TAG) != 0) {
        return -1;
    }

    return 0;
}

/* close i2c bus */
void close_i2c(void)
{
    /* lock mutex */
    if (pthread_mutex_lock(&i2c_mutex) != 0)
    {
        log_message(LOG_ERROR, TAG, "Failed to take mutex");
        return;
    }

    if (initialized)
    {
        close(i2c_ref); /* close the i2c connection */
        initialized = 0;
        log_message(LOG_INFO, TAG, "I²C connection closed");
    }

    /* unlock mutex */
    unlock_mutex_error(&i2c_mutex, TAG);
}

/* set the slave device address */
int i2c_set_slave(int address)
{
    int ret;

    /* lock mutex */
    if (pthread_mutex_lock(&i2c_mutex) != 0)
    {
        log_message(LOG_ERROR, TAG, "Failed to take mutex");
        return -1;
    }

    if (!initialized)
    {
        log_message(LOG_ERROR, TAG, "I2C not initialized");
        unlock_mutex_error(&i2c_mutex, TAG);
        return -1;
    }

    ret = ioctl(i2c_ref, I2C_SLAVE, address);
    if (ret < 0)
    {
        log_message(LOG_ERROR, TAG, "failed to set slave address %02X", address);
        unlock_mutex_error(&i2c_mutex, TAG);
        return -1;
    }

    log_message(LOG_INFO, TAG, "I2C slave set to address %02X", address);

    /* unlock mutex */
    if (unlock_mutex_error(&i2c_mutex, TAG) != 0) {
        return -1;
    }

    return 0;
}