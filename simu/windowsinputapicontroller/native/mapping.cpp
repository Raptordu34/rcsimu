// mapping.cpp (limpo)
// XInput + DirectInput simultâneo, sem logs/stdout

#include "mapping.h"

#include <windows.h>
#include <objbase.h>
#include <dinput.h>
#include <Xinput.h>
#include <cstdint>
#include <cstring>
#include <string>
#include <algorithm>
#include <cctype>
#include <cstdio> // For fprintf

#include <Xinput.h>
#pragma comment(lib, "Xinput9_1_0.lib") // pode ser xinput1_4 se der erro 


#pragma comment(lib, "dinput8.lib")
#pragma comment(lib, "dxguid.lib")
#pragma comment(lib, "Xinput.lib")

static IDirectInput8A*       g_dinput         = nullptr;

// Multiples périphériques DirectInput
static IDirectInputDevice8A* g_diDeviceWheel  = nullptr;  // Simucube 2 Pro (volant)
static IDirectInputDevice8A* g_diDevicePedals = nullptr;  // SC-Link Hub (pédales)
static IDirectInputDevice8A* g_diDeviceButtons = nullptr; // GTX2 (boutons)

static bool                 g_hasWheel       = false;
static bool                 g_hasPedals      = false;
static bool                 g_hasButtons     = false;
static bool                 g_comInitialized = false;

static void try_set_axis_range(IDirectInputDevice8A* dev)
{
    DIPROPRANGE diprg;
    diprg.diph.dwSize       = sizeof(DIPROPRANGE);
    diprg.diph.dwHeaderSize = sizeof(DIPROPHEADER);
    diprg.diph.dwHow        = DIPH_BYOFFSET;
    diprg.lMin = -32768;
    diprg.lMax =  32767;

    const DWORD axes[] = {
        DIJOFS_X, DIJOFS_Y, DIJOFS_Z,
        DIJOFS_RX, DIJOFS_RY, DIJOFS_RZ,
        DIJOFS_SLIDER(0), DIJOFS_SLIDER(1)
    };

    for (DWORD off : axes) {
        diprg.diph.dwObj = off;
        (void)dev->SetProperty(DIPROP_RANGE, &diprg.diph); // ignora falha
    }
}

// Helper: crée et configure un périphérique DirectInput
static IDirectInputDevice8A* create_and_setup_device(GUID guid)
{
    if (!g_dinput) return nullptr;

    IDirectInputDevice8A* dev = nullptr;
    if (FAILED(g_dinput->CreateDevice(guid, &dev, nullptr)) || !dev) {
        fprintf(stderr, "[NATIVE] CreateDevice failed\n");
        return nullptr;
    }

    if (FAILED(dev->SetDataFormat(&c_dfDIJoystick2))) {
        fprintf(stderr, "[NATIVE] SetDataFormat failed\n");
        dev->Release();
        return nullptr;
    }

    HWND hwnd = GetConsoleWindow();
    if (!hwnd) {
        // Fallback to desktop window if no console (e.g. service context)
        hwnd = GetDesktopWindow(); 
        fprintf(stderr, "[NATIVE] GetConsoleWindow() is NULL, using GetDesktopWindow() instead.\n");
    }

    if (FAILED(dev->SetCooperativeLevel(hwnd, DISCL_BACKGROUND | DISCL_NONEXCLUSIVE))) {
        fprintf(stderr, "[NATIVE] SetCooperativeLevel failed (hwnd=%p)\n", hwnd);
        dev->Release();
        return nullptr;
    }

    try_set_axis_range(dev);
    HRESULT hr = dev->Acquire();
    if (FAILED(hr)) {
        fprintf(stderr, "[NATIVE] Acquire failed (hr=0x%08X)\n", (unsigned int)hr);
        // Don't fail creation just because acquire failed (it might work later)
    }

    return dev;
}

// Helper: vérifie si le nom contient une sous-chaîne (case insensitive, version CHAR)
static bool contains_nocase_a(const char* haystack, const char* needle)
{
    if (!haystack || !needle) return false;

    std::string hay(haystack);
    std::string need(needle);

    // Convert to lowercase using Windows API
    CharLowerBuffA(&hay[0], (DWORD)hay.size());
    CharLowerBuffA(&need[0], (DWORD)need.size());

    return hay.find(need) != std::string::npos;
}

struct EnumDevicesCtx {
    IDirectInput8A* dinput;
    GUID wheelGuid;
    GUID pedalsGuid;
    GUID buttonsGuid;
    bool foundWheel;
    bool foundPedals;
    bool foundButtons;
};

static BOOL CALLBACK enum_devices_callback(const DIDEVICEINSTANCEA* inst, VOID* pvRef)
{
    auto* ctx = reinterpret_cast<EnumDevicesCtx*>(pvRef);
    if (!ctx || !inst) return DIENUM_CONTINUE;

    const char* name = inst->tszProductName;
    // fprintf(stderr, "[NATIVE] Enum device: '%s'\n", name); 

    // Identifier par nom
    if (contains_nocase_a(name, "Simucube 2") || contains_nocase_a(name, "SC2")) {
        fprintf(stderr, "[NATIVE] Found Wheel: %s\n", name);
        ctx->wheelGuid = inst->guidInstance;
        ctx->foundWheel = true;
    }
    else if (contains_nocase_a(name, "SC-Link") || contains_nocase_a(name, "SCLink")) {
        fprintf(stderr, "[NATIVE] Found Pedals: %s\n", name);
        ctx->pedalsGuid = inst->guidInstance;
        ctx->foundPedals = true;
    }
    else if (contains_nocase_a(name, "GTX") || contains_nocase_a(name, "Cube Controls")) {
        fprintf(stderr, "[NATIVE] Found Buttons: %s\n", name);
        ctx->buttonsGuid = inst->guidInstance;
        ctx->foundButtons = true;
    }

    return DIENUM_CONTINUE;  // Continue énumération
}

static bool find_and_create_all_directinput_devices()
{
    if (!g_dinput) return false;

    EnumDevicesCtx ctx;
    ctx.dinput = g_dinput;
    ctx.foundWheel = false;
    ctx.foundPedals = false;
    ctx.foundButtons = false;

    HRESULT hr = g_dinput->EnumDevices(
        DI8DEVCLASS_GAMECTRL,
        enum_devices_callback,
        &ctx,
        DIEDFL_ATTACHEDONLY
    );

    if (FAILED(hr)) return false;

    // Créer les périphériques trouvés
    if (ctx.foundWheel) {
        g_diDeviceWheel = create_and_setup_device(ctx.wheelGuid);
        g_hasWheel = (g_diDeviceWheel != nullptr);
    }

    if (ctx.foundPedals) {
        g_diDevicePedals = create_and_setup_device(ctx.pedalsGuid);
        g_hasPedals = (g_diDevicePedals != nullptr);
    }

    if (ctx.foundButtons) {
        g_diDeviceButtons = create_and_setup_device(ctx.buttonsGuid);
        g_hasButtons = (g_diDeviceButtons != nullptr);
    }

    return (g_hasWheel || g_hasPedals || g_hasButtons);
}

static float normalize_di_axis(LONG v)
{
    if (v >  32767) v =  32767;
    if (v < -32768) v = -32768;

    if (v >= 0) return (float)v / 32767.0f;
    return (float)v / 32768.0f; // já sai negativo
}

static inline float to01(float vMinus1to1)
{
    return (vMinus1to1 + 1.0f) * 0.5f;
}

// Poll un périphérique DirectInput spécifique et retourne son état
static bool poll_single_device(IDirectInputDevice8A* device, DIJOYSTATE2* outJs)
{
    if (!device || !outJs) return false;

    ZeroMemory(outJs, sizeof(DIJOYSTATE2));

    // Poll() avant GetDeviceState()
    HRESULT hr = device->Poll();
    if (FAILED(hr)) {
        // Log seulement si ce n'est pas juste "non acquis" (pour éviter le spam si ça boucle)
        // Mais ici on veut debug, donc on log tout pour l'instant
        fprintf(stderr, "[NATIVE] Poll() failed, hr=0x%08X\n", (unsigned int)hr);

        if (hr == DIERR_INPUTLOST || hr == DIERR_NOTACQUIRED) {
            hr = device->Acquire();
            if (FAILED(hr)) {
                fprintf(stderr, "[NATIVE] Acquire() recovery failed, hr=0x%08X\n", (unsigned int)hr);
                return false;
            }
            
            hr = device->Poll();
            if (FAILED(hr)) {
                fprintf(stderr, "[NATIVE] Poll() recovery failed, hr=0x%08X\n", (unsigned int)hr);
                return false;
            }
        } else {
            return false;
        }
    }

    hr = device->GetDeviceState(sizeof(DIJOYSTATE2), outJs);
    if (FAILED(hr)) {
        fprintf(stderr, "[NATIVE] GetDeviceState() failed, hr=0x%08X\n", (unsigned int)hr);
        
        if (hr == DIERR_INPUTLOST || hr == DIERR_NOTACQUIRED) {
            if (FAILED(device->Acquire())) return false;
            if (FAILED(device->Poll())) return false;

            ZeroMemory(outJs, sizeof(DIJOYSTATE2));
            if (FAILED(device->GetDeviceState(sizeof(DIJOYSTATE2), outJs))) return false;
        } else {
            return false;
        }
    }

    return true;
}

__declspec(dllexport) uint32_t BE_Init(void)
{
    fprintf(stderr, "[NATIVE] BE_Init called\n");
    if (g_dinput) return 1;

    HRESULT c = CoInitializeEx(NULL, COINIT_MULTITHREADED);
    g_comInitialized = (c == S_OK || c == S_FALSE);

    HINSTANCE hInst = GetModuleHandle(NULL);

    if (FAILED(DirectInput8Create(hInst, DIRECTINPUT_VERSION, IID_IDirectInput8A,
                                  (VOID**)&g_dinput, NULL)))
    {
        fprintf(stderr, "[NATIVE] DirectInput8Create failed\n");
        g_dinput = nullptr;
        return 0;
    }

    bool foundAny = find_and_create_all_directinput_devices();
    fprintf(stderr, "[NATIVE] BE_Init foundAny=%d (Wheel=%d, Pedals=%d, Buttons=%d)\n", 
        foundAny, g_hasWheel, g_hasPedals, g_hasButtons);

    return 1;
}

__declspec(dllexport) void BE_Shutdown(void)
{
    // Libérer les 3 périphériques DirectInput
    if (g_diDeviceWheel) {
        g_diDeviceWheel->Unacquire();
        g_diDeviceWheel->Release();
        g_diDeviceWheel = nullptr;
    }
    g_hasWheel = false;

    if (g_diDevicePedals) {
        g_diDevicePedals->Unacquire();
        g_diDevicePedals->Release();
        g_diDevicePedals = nullptr;
    }
    g_hasPedals = false;

    if (g_diDeviceButtons) {
        g_diDeviceButtons->Unacquire();
        g_diDeviceButtons->Release();
        g_diDeviceButtons = nullptr;
    }
    g_hasButtons = false;

    if (g_dinput) {
        g_dinput->Release();
        g_dinput = nullptr;
    }

    if (g_comInitialized) {
        CoUninitialize();
        g_comInitialized = false;
    }
}

__declspec(dllexport) uint32_t BE_PollState(BE_GamepadState* outState, uint32_t outStateSize)
{
    static int callCount = 0;
    bool debug = (callCount++ < 30); // Log first 30 calls

    if (debug) fprintf(stderr, "[NATIVE] BE_PollState #%d entry. Size=%d (Expected=%d)\n", callCount, outStateSize, (int)sizeof(BE_GamepadState));

    if (!outState || outStateSize < sizeof(BE_GamepadState)) {
        static bool logged = false;
        if (!logged) {
            fprintf(stderr, "[NATIVE] BE_PollState Error: Size mismatch. Java=%d, C++=%d\n", 
                    outStateSize, (int)sizeof(BE_GamepadState));
            logged = true;
        }
        return 0;
    }

    std::memset(outState, 0, sizeof(BE_GamepadState));
    outState->version = 1;
    outState->connected = 0;

    bool foundX = false;

    // ===== 1. XInput (pédales SC-Link Hub détectées comme XInput) =====
    for (DWORD i = 0; i < 4; ++i) {
        XINPUT_STATE xs;
        ZeroMemory(&xs, sizeof(xs));
        DWORD xr = XInputGetState(i, &xs);
        if (xr == ERROR_SUCCESS) {
            foundX = true;
            outState->connected = 1;
            if (debug) fprintf(stderr, "[NATIVE] XInput found on index %d\n", i);

            const float MAX_S = 32767.0f;

            // Sticks XInput (généralement pas utilisés pour sim racing)
            outState->lx = (float)xs.Gamepad.sThumbLX / MAX_S;
            outState->ly = (float)xs.Gamepad.sThumbLY / MAX_S;

            // RX/RY : Pédales SC-Link Hub (accélérateur et frein)
            outState->rx = (float)xs.Gamepad.sThumbRX / MAX_S;
            outState->ry = (float)xs.Gamepad.sThumbRY / MAX_S;

            outState->lt = xs.Gamepad.bLeftTrigger  / 255.0f;
            outState->rt = xs.Gamepad.bRightTrigger / 255.0f;
            outState->buttons = xs.Gamepad.wButtons;
            break;
        }
    }
    if (debug && !foundX) fprintf(stderr, "[NATIVE] No XInput device found.\n");

    // ===== 2. DirectInput - Volant Simucube 2 Pro =====
    if (g_hasWheel && g_diDeviceWheel) {
        if (debug) fprintf(stderr, "[NATIVE] Polling Wheel...\n");
        DIJOYSTATE2 jsWheel;
        if (poll_single_device(g_diDeviceWheel, &jsWheel)) {
            outState->connected = 1;
            if (debug) fprintf(stderr, "[NATIVE] Wheel poll OK\n");

            // Le volant utilise apparemment l'axe X pour la rotation principale (qui apparaissait comme diY avant)
            // Mapper la rotation vers di_lX
            outState->di_lX = (int16_t)jsWheel.lX;
            outState->di_lY = (int16_t)jsWheel.lY;
            outState->di_lZ = (int16_t)jsWheel.lZ;
        } else {
            if (debug) fprintf(stderr, "[NATIVE] Wheel poll FAILED. Releasing.\n");
            // Recréer le device si erreur
            g_diDeviceWheel->Unacquire();
            g_diDeviceWheel->Release();
            g_diDeviceWheel = nullptr;
            g_hasWheel = false;
        }
    } else if (debug) {
        fprintf(stderr, "[NATIVE] Wheel skipped (has=%d, ptr=%p)\n", g_hasWheel, g_diDeviceWheel);
    }

    // ===== 3. DirectInput - Pédales SC-Link Hub =====
    if (g_hasPedals && g_diDevicePedals) {
        if (debug) fprintf(stderr, "[NATIVE] Polling Pedals...\n");
        DIJOYSTATE2 jsPedals;
        if (poll_single_device(g_diDevicePedals, &jsPedals)) {
            outState->connected = 1;
            if (debug) fprintf(stderr, "[NATIVE] Pedals poll OK\n");

            // RX = accélérateur, RY = frein (d'après joy.cpl)
            // Mapper vers di_s0 et di_s1 pour ne pas écraser les axes XInput rx/ry (manette)
            outState->di_s0 = (int16_t)jsPedals.lRx; // throttle raw
            outState->di_s1 = (int16_t)jsPedals.lRy; // brake raw

            // Aussi stocker en raw pour debug (inchangé)
            outState->di_lRz = (int16_t)jsPedals.lRz;
            // outState->di_s0/s1 déjà utilisés ci-dessus
        } else {
            if (debug) fprintf(stderr, "[NATIVE] Pedals poll FAILED. Releasing.\n");
            g_diDevicePedals->Unacquire();
            g_diDevicePedals->Release();
            g_diDevicePedals = nullptr;
            g_hasPedals = false;
        }
    } else if (debug) {
        fprintf(stderr, "[NATIVE] Pedals skipped (has=%d, ptr=%p)\n", g_hasPedals, g_diDevicePedals);
    }

    // ===== 4. DirectInput - Boutons GTX2 =====
    if (g_hasButtons && g_diDeviceButtons) {
        if (debug) fprintf(stderr, "[NATIVE] Polling Buttons...\n");
        DIJOYSTATE2 jsButtons;
        if (poll_single_device(g_diDeviceButtons, &jsButtons)) {
            outState->connected = 1;
            if (debug) fprintf(stderr, "[NATIVE] Buttons poll OK\n");

            // Copier les boutons
            for (int i = 0; i < 32; ++i) {
                outState->di_buttons[i] = (jsButtons.rgbButtons[i] & 0x80) ? 1 : 0;
            }

            // Ajouter à la mask de boutons
            uint32_t btnmask = 0;
            if (outState->di_buttons[0]) btnmask |= 0x1000;
            if (outState->di_buttons[1]) btnmask |= 0x2000;
            outState->buttons |= btnmask;
        } else {
            if (debug) fprintf(stderr, "[NATIVE] Buttons poll FAILED. Releasing.\n");
            g_diDeviceButtons->Unacquire();
            g_diDeviceButtons->Release();
            g_diDeviceButtons = nullptr;
            g_hasButtons = false;
        }
    } else if (debug) {
        fprintf(stderr, "[NATIVE] Buttons skipped (has=%d, ptr=%p)\n", g_hasButtons, g_diDeviceButtons);
    }

    // Retenter la détection si tous les devices sont perdus
    if (!g_hasWheel && !g_hasPedals && !g_hasButtons) {
        if (debug) fprintf(stderr, "[NATIVE] All devices lost. Retrying enumeration...\n");
        (void)find_and_create_all_directinput_devices();
    }
    
    if (debug) fprintf(stderr, "[NATIVE] BE_PollState returning %d (connected=%d)\n", outState->connected ? 1 : 0, outState->connected);

    return outState->connected ? 1 : 0;
}
    //function for the vibration (XInput):
    
    static float clamp01(float v) {
    if (v < 0.f) return 0.f;
    if (v > 1.f) return 1.f;
    return v;
}

extern "C" __declspec(dllexport)
int be_set_xinput_vibration(int userIndex, float leftMotor01, float rightMotor01) {
    leftMotor01  = clamp01(leftMotor01);
    rightMotor01 = clamp01(rightMotor01);

    XINPUT_VIBRATION vib;
    vib.wLeftMotorSpeed  = (WORD)(leftMotor01  * 65535.0f);
    vib.wRightMotorSpeed = (WORD)(rightMotor01 * 65535.0f);

    return (int)XInputSetState((DWORD)userIndex, &vib);
}