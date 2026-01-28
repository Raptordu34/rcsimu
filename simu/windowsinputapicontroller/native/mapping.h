#pragma once

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct BE_GamepadState {
    uint32_t version;

    uint8_t  connected;
    uint8_t  _pad0[3];

    float lx, ly, rx, ry, lt, rt;

    uint32_t buttons;

    int16_t di_lX;
    int16_t di_lY;

    int16_t di_lZ;
    int16_t di_lRz;
    int16_t di_s0;
    int16_t di_s1;

    uint8_t di_buttons[32];

} BE_GamepadState;

__declspec(dllexport) uint32_t BE_Init(void);
__declspec(dllexport) void     BE_Shutdown(void);
__declspec(dllexport) uint32_t BE_PollState(BE_GamepadState* outState, uint32_t outStateSize);

// vibration (XInput):
__declspec(dllexport) int be_set_xinput_vibration(int userIndex, float leftMotor01, float rightMotor01);

#ifdef __cplusplus
}
#endif
