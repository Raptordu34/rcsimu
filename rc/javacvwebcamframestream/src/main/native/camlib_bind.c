
#include "camlib_bind.h"
#include "camlib.h"
#include <stdio.h>
#include <unistd.h>

// Support for multiple cameras (max 2: driver + assistant)
#define MAX_CAMERAS 2
static CameraContext* cameras[MAX_CAMERAS] = {NULL, NULL};

int init_camera_ex(int camera_index) {
    if (camera_index < 0 || camera_index >= MAX_CAMERAS) {
        printf("Erreur: Index caméra invalide: %d\n", camera_index);
        return -1;
    }

    printf("=== Init Camera %d ===\n", camera_index);

    cameras[camera_index] = cam_create_ex(camera_index);
    if (!cameras[camera_index]) {
        printf("Erreur: Impossible de créer le contexte caméra %d\n", camera_index);
        return -1;
    }

    printf("Initialisation caméra %d...\n", camera_index);
    if (!cam_init(cameras[camera_index])) {
        printf("Erreur: Initialisation caméra %d échouée\n", camera_index);
        cam_destroy(cameras[camera_index]);
        cameras[camera_index] = NULL;
        return -1;
    }

    CamConfig config;
    config.width = 854;
    config.height = 480;
    config.format = CAM_FMT_YUV420;
    config.fps = 60;

    printf("Configuration caméra %d (854x480 wide, YUV420, 60fps)...\n", camera_index);
    if (!cam_setup(cameras[camera_index], config)) {
        printf("Erreur: Configuration caméra %d échouée\n", camera_index);
        cam_destroy(cameras[camera_index]);
        cameras[camera_index] = NULL;
        return -1;
    }

    printf("Démarrage du flux caméra %d...\n", camera_index);
    if (!cam_start(cameras[camera_index])) {
        printf("Erreur: Impossible de démarrer le flux caméra %d\n", camera_index);
        cam_destroy(cameras[camera_index]);
        cameras[camera_index] = NULL;
        return -1;
    }

    printf("Camera %d initialisée avec succès\n", camera_index);
    return 0;
}

CamFrame* get_frame_ex(int camera_index) {
    if (camera_index < 0 || camera_index >= MAX_CAMERAS || !cameras[camera_index]) {
        return NULL;
    }

    CamFrame* frame = cam_get_frame(cameras[camera_index]);
    return frame;
}

void close_camera_ex(int camera_index) {
    if (camera_index < 0 || camera_index >= MAX_CAMERAS || !cameras[camera_index]) {
        return;
    }

    printf("Arrêt du flux caméra %d...\n", camera_index);
    cam_stop(cameras[camera_index]);
    cam_destroy(cameras[camera_index]);
    cameras[camera_index] = NULL;
}

// Legacy API - uses camera 0
int init_camera() {
    return init_camera_ex(0);
}

CamFrame* get_frame() {
    return get_frame_ex(0);
}

void close_camera() {
    close_camera_ex(0);
}
