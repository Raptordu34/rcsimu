/**
 * @file dbox_controller_wrapper.h
 * @brief Wrapper C pour exposer SimpleDboxController a Java via Panama FFM
 *
 * Ce wrapper fournit une interface C (sans name mangling C++) pour permettre
 * a Java d'appeler les methodes de SimpleDboxController via l'API Panama FFM.
 *
 * ARCHITECTURE:
 *   Java (DboxController.java)
 *       |
 *       | Panama FFM (MethodHandle)
 *       v
 *   dbox_controller_wrapper.dll  <-- CE FICHIER
 *       |
 *       | Appels C++
 *       v
 *   SimpleDboxAPI.dll (SimpleDboxController)
 *       |
 *       v
 *   dbxLive64.dll (SDK DBOX officiel)
 *
 * @author Migration UDP -> FFM
 * @date 2024
 */

#ifndef DBOX_CONTROLLER_WRAPPER_H
#define DBOX_CONTROLLER_WRAPPER_H

#ifdef __cplusplus
extern "C" {
#endif

// ===========================================================================
// MACROS D'EXPORT DLL
// ===========================================================================

#ifdef DBOX_CONTROLLER_WRAPPER_EXPORTS
    #define DBOX_API __declspec(dllexport)
#else
    #define DBOX_API __declspec(dllimport)
#endif

// ===========================================================================
// GESTION DU CYCLE DE VIE
// ===========================================================================

/**
 * @brief Cree une nouvelle instance de SimpleDboxController
 * @return Pointeur opaque vers le controleur, ou NULL en cas d'erreur
 */
DBOX_API void* DBOX_Controller_Create(void);

/**
 * @brief Detruit une instance de SimpleDboxController
 * @param handle Pointeur opaque retourne par DBOX_Controller_Create()
 */
DBOX_API void DBOX_Controller_Destroy(void* handle);

// ===========================================================================
// CONNEXION ET CONTROLE
// ===========================================================================

/**
 * @brief Connecte et initialise la plateforme DBOX
 * @param handle Pointeur opaque vers le controleur
 * @return true si connexion reussie, false sinon
 */
DBOX_API bool DBOX_Controller_Connect(void* handle);

/**
 * @brief Deconnecte et libere les ressources DBOX
 * @param handle Pointeur opaque vers le controleur
 */
DBOX_API void DBOX_Controller_Disconnect(void* handle);

/**
 * @brief Demarre la simulation avec fade-in progressif
 * @param handle Pointeur opaque vers le controleur
 * @return true si demarrage reussi, false sinon
 */
DBOX_API bool DBOX_Controller_Start(void* handle);

/**
 * @brief Arrete la simulation avec fade-out progressif
 * @param handle Pointeur opaque vers le controleur
 * @return true si arret reussi, false sinon
 */
DBOX_API bool DBOX_Controller_Stop(void* handle);

// ===========================================================================
// CONTROLE DES MOUVEMENTS
// ===========================================================================

/**
 * @brief Met a jour TOUS les parametres de mouvement
 * @param handle Pointeur opaque vers le controleur
 * @param roll   Roulis normalise [-1.0, +1.0]
 * @param pitch  Tangage normalise [-1.0, +1.0]
 * @param heave  Pilonnement normalise [-1.0, +1.0]
 * @param rpm    Regime moteur [0, 10000] RPM
 * @param torque Couple moteur [0, 1000] N.m
 */
DBOX_API void DBOX_Controller_Update(void* handle,
                                      float roll, float pitch, float heave,
                                      float rpm, float torque);

/**
 * @brief Definit uniquement les mouvements (roll, pitch, heave)
 */
DBOX_API void DBOX_Controller_SetMotion(void* handle,
                                         float roll, float pitch, float heave);

/**
 * @brief Definit uniquement les vibrations moteur
 */
DBOX_API void DBOX_Controller_SetVibration(void* handle, float rpm, float torque);

/**
 * @brief Retourne immediatement a la position neutre
 */
DBOX_API void DBOX_Controller_ResetToNeutral(void* handle);

// ===========================================================================
// INTERROGATION D'ETAT
// ===========================================================================

/**
 * @brief Verifie si la plateforme est connectee
 * @return true si connecte, false sinon
 */
DBOX_API bool DBOX_Controller_IsConnected(void* handle);

/**
 * @brief Verifie si la simulation est en cours
 * @return true si running, false sinon
 */
DBOX_API bool DBOX_Controller_IsRunning(void* handle);

/**
 * @brief Recupere le dernier message d'erreur
 * @return Pointeur vers la chaine d'erreur
 */
DBOX_API const char* DBOX_Controller_GetLastError(void* handle);

// ===========================================================================
// CONFIGURATION AVANCEE (optionnel, avant connect)
// ===========================================================================

DBOX_API void DBOX_Controller_SetMasterGain(void* handle, float gain_db);
DBOX_API void DBOX_Controller_SetEngineRange(void* handle, float idle_rpm, float max_rpm);
DBOX_API void DBOX_Controller_SetMaxTorque(void* handle, float max_torque);

#ifdef __cplusplus
}
#endif

#endif // DBOX_CONTROLLER_WRAPPER_H
