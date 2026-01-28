#ifndef CAMLIBBIND_H_
#define CAMLIBBIND_H_

#include "camlib.h"

// Multi-camera support: each camera has its own context
// camera_index: 0 = driver camera, 1 = assistant camera
int init_camera_ex(int camera_index);
CamFrame* get_frame_ex(int camera_index);
void close_camera_ex(int camera_index);

// Legacy single-camera API (uses camera 0)
int init_camera();
CamFrame* get_frame();
void close_camera();

#endif