#ifndef I2C_H_
#define I2C_H_

int i2c_write_byte(int reg, int value);
int i2c_read_byte(int reg);
int init_i2c();
void close_i2c();
int i2c_set_slave(int address);

#endif /* I2C_H_ */
