#include "camlib.h"
#include "CameraLib.hpp"
#include <cstring>
#include <iostream>

struct CameraContext {
    CameraLib* lib;
    CamFrame current_frame; // Cache frame info
    int camera_index;       // Which camera to use (0, 1, ...)
};

extern "C" {

CameraContext* cam_create() {
    return cam_create_ex(0);
}

CameraContext* cam_create_ex(int camera_index) {
    CameraContext* ctx = new CameraContext;
    ctx->lib = new CameraLib();
    ctx->camera_index = camera_index;
    std::memset(&ctx->current_frame, 0, sizeof(CamFrame));
    return ctx;
}

void cam_destroy(CameraContext* ctx) {
    if (ctx) {
        delete ctx->lib;
        delete ctx;
    }
}

int cam_init(CameraContext* ctx) {
    if (!ctx || !ctx->lib) return 0;
    return ctx->lib->init(ctx->camera_index) ? 1 : 0;
}

int cam_setup(CameraContext* ctx, CamConfig config) {
    if (!ctx || !ctx->lib) return 0;
    return ctx->lib->configure(config) ? 1 : 0;
}

int cam_start(CameraContext* ctx) {
    if (!ctx || !ctx->lib) return 0;
    return ctx->lib->start() ? 1 : 0;
}

void cam_stop(CameraContext* ctx) {
    if (!ctx || !ctx->lib) return;
    ctx->lib->stop();
    std::memset(&ctx->current_frame, 0, sizeof(CamFrame));
}

CamFrame* cam_get_frame(CameraContext* ctx) {
    if (!ctx || !ctx->lib) return NULL;
    
    // Pass pointer to internal struct to be filled
    if (ctx->lib->getFrame(&ctx->current_frame)) {
        return &ctx->current_frame;
    }
    return NULL;
}

int cam_take_photo(CameraContext* ctx, const char* filename, int width, int height) {
    if (!ctx || !ctx->lib) return 0;
    return ctx->lib->takePhoto(filename, width, height) ? 1 : 0;
}

}
