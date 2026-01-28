#include "CameraLib.hpp"
#include <iostream>
#include <thread>
#include <chrono>
#include <sys/mman.h>
#include <unistd.h>
#include <cstring> 
#include <map>

extern "C" {
#include <jpeglib.h>
#include <turbojpeg.h>
}

bool encodeYUVtoJPEGInMemoryTurbo(const uint8_t* yuv, int width, int height,
    std::vector<uint8_t>& outJPEG, int quality = 75)
{
    tjhandle tj = tjInitCompress();
    if (!tj) return false;

    unsigned char* jpegBuf = nullptr;
    unsigned long jpegSize = 0;

    // width = largeur en pixels, align = 0 pour "pas d’alignement spécial"
    int ret = tjCompressFromYUV(
    tj,
    yuv,           // YUV420 contigu: Y plane | U plane | V plane
    width,         // largeur image
    1,             // pad: 1 = no padding (byte alignment) - 0 is INVALID
    height,        // hauteur image
    TJSAMP_420,    // subsampling
    &jpegBuf,      // output JPEG
    &jpegSize,     // taille output
    quality,       // qualité JPEG
    TJFLAG_FASTDCT // option
    );

    if (ret != 0) {
    std::cerr << "TurboJPEG error: " << tjGetErrorStr() << std::endl;
    tjDestroy(tj);
    return false;
    }

    outJPEG.assign(jpegBuf, jpegBuf + jpegSize);

    tjFree(jpegBuf);
    tjDestroy(tj);
    return true;
}



bool encodeYUVtoJPEGInMemory(const uint8_t* yuv, int width, int height,
                             std::vector<uint8_t>& outJPEG, int quality = 90) {
    struct jpeg_compress_struct cinfo;
    struct jpeg_error_mgr jerr;
    cinfo.err = jpeg_std_error(&jerr);
    jpeg_create_compress(&cinfo);

    unsigned char* mem = nullptr;
    unsigned long mem_size = 0;
    jpeg_mem_dest(&cinfo, &mem, &mem_size); // <-- encode en mémoire

    cinfo.image_width = width;
    cinfo.image_height = height;
    cinfo.input_components = 3;
    cinfo.in_color_space = JCS_RGB;

    jpeg_set_defaults(&cinfo);
    jpeg_set_quality(&cinfo, quality, TRUE);
    jpeg_start_compress(&cinfo, TRUE);

    JSAMPROW row_pointer[1];
    std::vector<uint8_t> rgb(width*3);

    // Offsets for YUV420 planar (Standard I420 is Y, U, V)
    const uint8_t* Y_plane = yuv;
    const uint8_t* U_plane = yuv + (width * height);
    const uint8_t* V_plane = yuv + (width * height) + (width * height / 4);

    while (cinfo.next_scanline < cinfo.image_height) {
        for (int x = 0; x < width; ++x) {
            int y_idx = cinfo.next_scanline * width + x;
            int uv_idx = (cinfo.next_scanline / 2) * (width / 2) + (x / 2);

            uint8_t Y = Y_plane[y_idx];
            // Simple grayscale for now if only Y provided, or implement conversion
            // To properly convert YUV to RGB here we need the U and V
            // For valid JPEG from YUV420 inputs, using libjpeg raw input is better, 
            // but here we manually convert to RGB for simplicity of the "save" function structure.
            
            // Basic conversion
            uint8_t U = U_plane[uv_idx];
            uint8_t V = V_plane[uv_idx];
            
            int c = Y - 16;
            int d = U - 128;
            int e = V - 128;

            int R = (298 * c           + 409 * e + 128) >> 8;
            int G = (298 * c - 100 * d - 208 * e + 128) >> 8;
            int B = (298 * c + 516 * d           + 128) >> 8;

            rgb[3*x+0] = (uint8_t)(R < 0 ? 0 : (R > 255 ? 255 : R));
            rgb[3*x+1] = (uint8_t)(G < 0 ? 0 : (G > 255 ? 255 : G));
            rgb[3*x+2] = (uint8_t)(B < 0 ? 0 : (B > 255 ? 255 : B));
        }
        row_pointer[0] = rgb.data();
        jpeg_write_scanlines(&cinfo, row_pointer, 1);
    }

    jpeg_finish_compress(&cinfo);

    outJPEG.assign(mem, mem + mem_size); // copie dans vector
    free(mem);
    jpeg_destroy_compress(&cinfo);

    return true;
}

// Sauvegarde simple YUV420 -> JPEG (Helper)
// Note: yuv buffer must contain Y, U, V contiguous
static bool saveYUV420toJPEG(const uint8_t* yuv, int width, int height, const char* filename, int quality = 90) {
    struct jpeg_compress_struct cinfo;
    struct jpeg_error_mgr jerr;

    cinfo.err = jpeg_std_error(&jerr);
    jpeg_create_compress(&cinfo);

    FILE* outfile = fopen(filename, "wb");
    if (!outfile) return false;

    jpeg_stdio_dest(&cinfo, outfile);

    cinfo.image_width = width;
    cinfo.image_height = height;
    cinfo.input_components = 3;
    cinfo.in_color_space = JCS_RGB;

    jpeg_set_defaults(&cinfo);
    jpeg_set_quality(&cinfo, quality, TRUE);
    jpeg_start_compress(&cinfo, TRUE);

    JSAMPROW row_pointer[1];
    std::vector<uint8_t> rgb(width * 3);

    // Offsets for YUV420 planar (Standard I420 is Y, U, V)
    const uint8_t* Y_plane = yuv;
    const uint8_t* U_plane = yuv + (width * height);
    const uint8_t* V_plane = yuv + (width * height) + (width * height / 4);

    while (cinfo.next_scanline < cinfo.image_height) {
        for (int x = 0; x < width; ++x) {
            int y_idx = cinfo.next_scanline * width + x;
            int uv_idx = (cinfo.next_scanline / 2) * (width / 2) + (x / 2);

            uint8_t Y = Y_plane[y_idx];
            // Simple grayscale for now if only Y provided, or implement conversion
            // To properly convert YUV to RGB here we need the U and V
            // For valid JPEG from YUV420 inputs, using libjpeg raw input is better, 
            // but here we manually convert to RGB for simplicity of the "save" function structure.
            
            // Basic conversion
            uint8_t U = U_plane[uv_idx];
            uint8_t V = V_plane[uv_idx];
            
            int c = Y - 16;
            int d = U - 128;
            int e = V - 128;

            int R = (298 * c           + 409 * e + 128) >> 8;
            int G = (298 * c - 100 * d - 208 * e + 128) >> 8;
            int B = (298 * c + 516 * d           + 128) >> 8;

            rgb[3*x+0] = (uint8_t)(R < 0 ? 0 : (R > 255 ? 255 : R));
            rgb[3*x+1] = (uint8_t)(G < 0 ? 0 : (G > 255 ? 255 : G));
            rgb[3*x+2] = (uint8_t)(B < 0 ? 0 : (B > 255 ? 255 : B));
        }
        row_pointer[0] = rgb.data();
        jpeg_write_scanlines(&cinfo, row_pointer, 1);
    }

    jpeg_finish_compress(&cinfo);
    fclose(outfile);
    jpeg_destroy_compress(&cinfo);
    return true;
}

// === GlobalCameraManager Singleton ===

GlobalCameraManager& GlobalCameraManager::instance() {
    static GlobalCameraManager inst;
    return inst;
}

GlobalCameraManager::GlobalCameraManager() : started_(false) {
    std::lock_guard<std::mutex> lock(mutex_);
    cm_.start();
    started_ = true;
    std::cout << "GlobalCameraManager: started, " << cm_.cameras().size() << " camera(s) detected" << std::endl;
    for (size_t i = 0; i < cm_.cameras().size(); i++) {
        std::cout << "  [" << i << "] " << cm_.cameras()[i]->id() << std::endl;
    }
}

GlobalCameraManager::~GlobalCameraManager() {
    std::lock_guard<std::mutex> lock(mutex_);
    if (started_) {
        cm_.stop();
        started_ = false;
    }
}

libcamera::CameraManager& GlobalCameraManager::get() {
    return cm_;
}

// === CameraLib ===

CameraLib::CameraLib()
    : stream_(nullptr),
      completed_request_(nullptr),
      started_(false),
      target_fps_(60) {}

CameraLib::~CameraLib() {
    stop();

    // Explicit destruction order to avoid SegFaults
    allocator_.reset(); // Destroy allocator first

    if (camera_) {
        camera_->release();
        camera_.reset();
    }
    // Don't stop the global CameraManager here
}

bool CameraLib::init(int camera_index) {
    auto& cm = GlobalCameraManager::instance().get();

    size_t num_cameras = cm.cameras().size();
    if (num_cameras == 0) {
        std::cerr << "Aucune caméra détectée !" << std::endl;
        return false;
    }

    if (camera_index < 0 || static_cast<size_t>(camera_index) >= num_cameras) {
        std::cerr << "Index caméra " << camera_index << " invalide (max: " << (num_cameras - 1) << ")" << std::endl;
        return false;
    }

    camera_ = cm.cameras()[camera_index];
    if (camera_->acquire()) {
        std::cerr << "Impossible d'acquérir la caméra " << camera_index << std::endl;
        return false;
    }
    std::cout << "Caméra " << camera_index << " acquise : " << camera_->id() << std::endl;
    allocator_ = std::make_unique<libcamera::FrameBufferAllocator>(camera_);
    return true;
}

bool CameraLib::configure(const CamConfig& config) {
    if (!camera_) return false;

    stop();

    // Store target FPS (default to 60 if not specified)
    target_fps_ = (config.fps > 0) ? config.fps : 60;

    // Use VideoRecording role for better FPS (prioritizes throughput over raw quality)
    config_ = camera_->generateConfiguration({ libcamera::StreamRole::VideoRecording });
    if (!config_) return false;

    libcamera::StreamConfiguration &streamConfig = config_->at(0);

    // Force YUV420 for now as our main supported format
    streamConfig.pixelFormat = libcamera::formats::YUV420;

    streamConfig.size.width = config.width;
    streamConfig.size.height = config.height;

    if (camera_->configure(config_.get()) != 0) {
        std::cerr << "Erreur configuration flux" << std::endl;
        return false;
    }

    stream_ = streamConfig.stream();
    return true;
}

bool CameraLib::start() {
    if (!camera_ || !stream_) return false;
    if (started_) return true;

    if (allocator_->allocate(stream_) < 0) {
        std::cerr << "Erreur allocation buffer" << std::endl;
        return false;
    }

    // Set framerate via FrameDurationLimits control
    // Duration in microseconds: 1000000 / fps
    int64_t frame_duration_us = 1000000 / target_fps_;
    libcamera::ControlList controls;
    controls.set(libcamera::controls::FrameDurationLimits,
                 libcamera::Span<const int64_t, 2>({frame_duration_us, frame_duration_us}));

    if (camera_->start(&controls) != 0) {
        std::cerr << "Impossible de démarrer la caméra" << std::endl;
        return false;
    }

    std::cout << "Caméra démarrée @ " << target_fps_ << " FPS (frame duration: "
              << frame_duration_us << " µs)" << std::endl;

    started_ = true;
    camera_->requestCompleted.connect(this, &CameraLib::requestComplete);
    return true;
}

void CameraLib::stop() {
    if (camera_ && started_) {
        camera_->requestCompleted.disconnect(this, &CameraLib::requestComplete);
        camera_->stop();
        started_ = false;
    }
    if (allocator_ && stream_) {
        allocator_->free(stream_);
    }
    config_.reset(); 
    stream_ = nullptr;
}

void CameraLib::requestComplete(libcamera::Request *request) {
    if (request->status() == libcamera::Request::RequestComplete) {
        std::lock_guard<std::mutex> lock(mutex_);
        completed_request_ = request;
        frame_ready_ = true;
    }
}

bool CameraLib::getFrame(CamFrame* frame) {
    if (!started_ || !stream_) return false;

    auto &buffers = allocator_->buffers(stream_);
    if (buffers.empty()) return false;

    auto request = camera_->createRequest();
    if (!request) return false;

    request->addBuffer(stream_, buffers[0].get());

    frame_ready_ = false;
    completed_request_ = nullptr;
    
    camera_->queueRequest(request.get());

    int timeout_ms = 2000; 
    while (!frame_ready_ && timeout_ms > 0) {
        std::this_thread::sleep_for(std::chrono::milliseconds(1));
        timeout_ms -= 1;
    }

    if (!frame_ready_) return false;

    // Process Buffer: Copy all planes to a contiguous internal buffer removing stride padding
    const auto &planes = buffers[0]->planes();
    unsigned int stride = config_->at(0).stride;
    unsigned int width = config_->at(0).size.width;
    unsigned int height = config_->at(0).size.height;
    
    // Calculate expected compact I420 size (no padding)
    // Y = w*h, U = (w/2)*(h/2), V = (w/2)*(h/2) -> Total = w*h * 1.5
    size_t dense_size = width * height * 3 / 2;

    if (frame_buffer_.size() != dense_size) {
        frame_buffer_.resize(dense_size);
    }
    
    // Map the entire buffer once (all planes share the same FD on RPi)
    // We need to know the total span of the buffer
    size_t max_offset = 0;
    for (const auto &p : planes) {
        if (p.offset + p.length > max_offset) max_offset = p.offset + p.length;
    }

    void *base_data = mmap(NULL, max_offset, PROT_READ, MAP_SHARED, planes[0].fd.get(), 0);
    if (base_data == MAP_FAILED) {
        return false;
    }

    // Explicit Destination Offsets for I420 (Y, then U, then V)
    size_t offset_Y = 0;
    size_t offset_U = width * height;
    size_t offset_V = offset_U + (width / 2) * (height / 2);
    
    for (unsigned int i = 0; i < planes.size(); i++) {
        const auto &plane = planes[i];
        
        // Pointer to the start of THIS plane in the mapped buffer
        // CRITICAL FIX: Use plane.offset to jump to correct memory section
        uint8_t* src_ptr = static_cast<uint8_t*>(base_data) + plane.offset;
        
        unsigned int w = (i == 0) ? width : width / 2;
        unsigned int h = (i == 0) ? height : height / 2;
        unsigned int s = (i == 0) ? stride : stride / 2;
        
        size_t current_dest_offset = 0;
        if (i == 0) current_dest_offset = offset_Y;
        else if (i == 1) current_dest_offset = offset_U;
        else if (i == 2) current_dest_offset = offset_V;

        for (unsigned int row = 0; row < h; row++) {
            std::memcpy(frame_buffer_.data() + current_dest_offset, src_ptr + (row * s), w);
            current_dest_offset += w;
        }
    }

    munmap(base_data, max_offset);

    // --- Conversion YUV420 → JPEG en mémoire ---
    jpeg_frame_.clear(); 
    // Quality set to 50 for performance
    if (!encodeYUVtoJPEGInMemoryTurbo(frame_buffer_.data(),
                            width,
                            height,
                            jpeg_frame_,
                            50)) {
         encodeYUVtoJPEGInMemory(frame_buffer_.data(),
                            width,
                            height,
                            jpeg_frame_,
                            50);
    }

    // --- Remplir la struct CamFrame pour Java ---
    frame->data = jpeg_frame_.data();
    frame->size = jpeg_frame_.size();
    frame->width = config_->at(0).size.width;
    frame->height = config_->at(0).size.height;
    frame->timestamp_us = 0;


    return true;
}

bool CameraLib::takePhoto(const std::string &filename, int width, int height) {
    CamConfig cfg;
    cfg.width = width;
    cfg.height = height;
    cfg.format = CAM_FMT_YUV420;
    cfg.fps = 0;

    if (!configure(cfg)) return false;
    if (!start()) return false;

    CamFrame frame;
    bool res = getFrame(&frame);
    
    if (res) {
        res = saveYUV420toJPEG(frame.data, width, height, filename.c_str());
    }

    stop();
    return res;
}