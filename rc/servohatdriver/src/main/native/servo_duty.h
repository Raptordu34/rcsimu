#ifndef SERVO_DUTY_H_
#define SERVO_DUTY_H_

int set_servo_duty_direction(int angle_percent);
int set_motor_duty_esc_lrp(int speed_percent);
int set_servo_pulse_camera_hor(float pulse_us);
int set_servo_pulse_camera_ver(float pulse_us);

int set_servo_duty_direction_manual(int duty);
int set_motor_duty_esc_lrp_manual(int duty);

#endif /* SERVO_DUTY_H_ */
