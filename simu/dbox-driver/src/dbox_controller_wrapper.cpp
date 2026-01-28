/**
 * @file dbox_controller_wrapper.cpp
 * @brief Implementation du wrapper C pour SimpleDboxController
 *
 * Ce fichier implemente les fonctions C exportees qui wrappent la classe
 * C++ SimpleDboxController pour l'acces via Java Panama FFM.
 *
 * COMPILATION:
 *   Voir scripts/compile_wrapper.bat
 *
 * @author Migration UDP -> FFM
 * @date 2024
 *
 * NOTE: Logging convention:
 * All output to std::cout/std::cerr must be prefixed with "[dbox_controller_wrapper] "
 */

#define DBOX_CONTROLLER_WRAPPER_EXPORTS
#include "dbox_controller_wrapper.h"
#include "SimpleDboxAPI.h"

#include <iostream>
#include <string>

// ===========================================================================
// BUFFER STATIQUE POUR LES MESSAGES D'ERREUR
// ===========================================================================

static thread_local char g_errorBuffer[1024] = "";

// ===========================================================================
// HELPER: Cast securise du handle
// ===========================================================================

static SimpleDboxController* getController(void* handle) {
    if (handle == nullptr) {
        std::cerr << "[dbox_controller_wrapper] ERROR: NULL handle" << std::endl;
        return nullptr;
    }
    return static_cast<SimpleDboxController*>(handle);
}

// ===========================================================================
// GESTION DU CYCLE DE VIE
// ===========================================================================

DBOX_API void* DBOX_Controller_Create(void) {
    try {
        std::cout << "[dbox_controller_wrapper] Creating SimpleDboxController..." << std::endl;
        SimpleDboxController* controller = new SimpleDboxController();
        std::cout << "[dbox_controller_wrapper] Controller created at " << controller << std::endl;
        return static_cast<void*>(controller);
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in Create: " << e.what() << std::endl;
        return nullptr;
    }
    catch (...) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Unknown exception in Create" << std::endl;
        return nullptr;
    }
}

DBOX_API void DBOX_Controller_Destroy(void* handle) {
    if (handle == nullptr) {
        return;
    }

    try {
        SimpleDboxController* controller = getController(handle);
        if (controller != nullptr) {
            std::cout << "[dbox_controller_wrapper] Destroying controller at " << controller << std::endl;
            delete controller;
            std::cout << "[dbox_controller_wrapper] Controller destroyed" << std::endl;
        }
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in Destroy: " << e.what() << std::endl;
    }
    catch (...) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Unknown exception in Destroy" << std::endl;
    }
}

// ===========================================================================
// CONNEXION ET CONTROLE
// ===========================================================================

DBOX_API bool DBOX_Controller_Connect(void* handle) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return false;
    }

    try {
        std::cout << "[dbox_controller_wrapper] Connecting..." << std::endl;
        bool result = controller->connect();
        std::cout << "[dbox_controller_wrapper] Connect result: " << (result ? "SUCCESS" : "FAILED") << std::endl;
        return result;
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in Connect: " << e.what() << std::endl;
        return false;
    }
}

DBOX_API void DBOX_Controller_Disconnect(void* handle) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return;
    }

    try {
        std::cout << "[dbox_controller_wrapper] Disconnecting..." << std::endl;
        controller->disconnect();
        std::cout << "[dbox_controller_wrapper] Disconnected" << std::endl;
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in Disconnect: " << e.what() << std::endl;
    }
}

DBOX_API bool DBOX_Controller_Start(void* handle) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return false;
    }

    try {
        std::cout << "[dbox_controller_wrapper] Starting simulation..." << std::endl;
        bool result = controller->start();
        std::cout << "[dbox_controller_wrapper] Start result: " << (result ? "SUCCESS" : "FAILED") << std::endl;
        return result;
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in Start: " << e.what() << std::endl;
        return false;
    }
}

DBOX_API bool DBOX_Controller_Stop(void* handle) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return false;
    }

    try {
        std::cout << "[dbox_controller_wrapper] Stopping simulation..." << std::endl;
        bool result = controller->stop();
        std::cout << "[dbox_controller_wrapper] Stop result: " << (result ? "SUCCESS" : "FAILED") << std::endl;
        return result;
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in Stop: " << e.what() << std::endl;
        return false;
    }
}

// ===========================================================================
// CONTROLE DES MOUVEMENTS
// ===========================================================================

DBOX_API void DBOX_Controller_Update(void* handle,
                                      float roll, float pitch, float heave,
                                      float rpm, float torque) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return;
    }

    try {
        controller->update(roll, pitch, heave, rpm, torque);
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in Update: " << e.what() << std::endl;
    }
}

DBOX_API void DBOX_Controller_SetMotion(void* handle,
                                         float roll, float pitch, float heave) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return;
    }

    try {
        controller->setMotion(roll, pitch, heave);
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in SetMotion: " << e.what() << std::endl;
    }
}

DBOX_API void DBOX_Controller_SetVibration(void* handle, float rpm, float torque) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return;
    }

    try {
        controller->setVibration(rpm, torque);
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in SetVibration: " << e.what() << std::endl;
    }
}

DBOX_API void DBOX_Controller_ResetToNeutral(void* handle) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return;
    }

    try {
        std::cout << "[dbox_controller_wrapper] Resetting to neutral..." << std::endl;
        controller->resetToNeutral();
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in ResetToNeutral: " << e.what() << std::endl;
    }
}

// ===========================================================================
// INTERROGATION D'ETAT
// ===========================================================================

DBOX_API bool DBOX_Controller_IsConnected(void* handle) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return false;
    }

    try {
        return controller->isConnected();
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in IsConnected: " << e.what() << std::endl;
        return false;
    }
}

DBOX_API bool DBOX_Controller_IsRunning(void* handle) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return false;
    }

    try {
        return controller->isRunning();
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in IsRunning: " << e.what() << std::endl;
        return false;
    }
}

DBOX_API const char* DBOX_Controller_GetLastError(void* handle) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        strncpy_s(g_errorBuffer, sizeof(g_errorBuffer), "Invalid controller handle (NULL)", _TRUNCATE);
        return g_errorBuffer;
    }

    try {
        std::string error = controller->getLastError();
        strncpy_s(g_errorBuffer, sizeof(g_errorBuffer), error.c_str(), _TRUNCATE);
        return g_errorBuffer;
    }
    catch (const std::exception& e) {
        strncpy_s(g_errorBuffer, sizeof(g_errorBuffer), e.what(), _TRUNCATE);
        return g_errorBuffer;
    }
}

// ===========================================================================
// CONFIGURATION AVANCEE
// ===========================================================================

DBOX_API void DBOX_Controller_SetMasterGain(void* handle, float gain_db) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return;
    }

    try {
        std::cout << "[dbox_controller_wrapper] Setting master gain to " << gain_db << " dB" << std::endl;
        controller->setMasterGain(gain_db);
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in SetMasterGain: " << e.what() << std::endl;
    }
}

DBOX_API void DBOX_Controller_SetEngineRange(void* handle, float idle_rpm, float max_rpm) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return;
    }

    try {
        std::cout << "[dbox_controller_wrapper] Setting engine range: idle=" << idle_rpm
                  << " RPM, max=" << max_rpm << " RPM" << std::endl;
        controller->setEngineRange(idle_rpm, max_rpm);
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in SetEngineRange: " << e.what() << std::endl;
    }
}

DBOX_API void DBOX_Controller_SetMaxTorque(void* handle, float max_torque) {
    SimpleDboxController* controller = getController(handle);
    if (controller == nullptr) {
        return;
    }

    try {
        std::cout << "[dbox_controller_wrapper] Setting max torque to " << max_torque << " N.m" << std::endl;
        controller->setMaxTorque(max_torque);
    }
    catch (const std::exception& e) {
        std::cerr << "[dbox_controller_wrapper] ERROR: Exception in SetMaxTorque: " << e.what() << std::endl;
    }
}
