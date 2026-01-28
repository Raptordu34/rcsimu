#ifndef CAMLIB_H
#define CAMLIB_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct CameraContext CameraContext;

typedef enum {
    CAM_FMT_YUV420 = 0,
    CAM_FMT_NV12   = 1,
    CAM_FMT_MJPEG  = 2,
    CAM_FMT_RGB    = 3
} CamPixelFormat;

typedef struct {
    int32_t width;
    int32_t height;
    int32_t fps;
    int32_t format; // CamPixelFormat
} CamConfig;

typedef struct {
    uint8_t* data;        // appartient à la lib C
    uint32_t size;
    uint64_t timestamp_us;
    int32_t width;
    int32_t height;
    int32_t format;
} CamFrame;

// Crée l'instance de la caméra (utilise la première caméra disponible)
CameraContext* cam_create();

// Crée l'instance de la caméra avec un index spécifique
CameraContext* cam_create_ex(int camera_index);

// Libère la mémoire
void cam_destroy(CameraContext* ctx);

// Initialise le CameraManager et détecte la caméra (retourne 1 si ok)
int cam_init(CameraContext* ctx);

// Configure la caméra (doit être appelé après init, avant start)
int cam_setup(CameraContext* ctx, CamConfig config);

// Démarre le flux (retourne 1 si ok)
int cam_start(CameraContext* ctx);

// Arrête le flux
void cam_stop(CameraContext* ctx);

// Récupère une frame (bloquant). Retourne NULL en cas d'erreur ou timeout.
// La frame retournée est valide jusqu'au prochain appel à cam_get_frame ou cam_stop.
CamFrame* cam_get_frame(CameraContext* ctx);

// Prend une photo (mode "one-shot" legacy, configure/start/stop en interne si besoin)
int cam_take_photo(CameraContext* ctx, const char* filename, int width, int height);

#ifdef __cplusplus
}
#endif

#endif
