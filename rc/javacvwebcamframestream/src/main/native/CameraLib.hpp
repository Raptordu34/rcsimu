#pragma once
#include <string>
#include <memory>
#include <vector>
#include <atomic>
#include <mutex>
#include <libcamera/libcamera.h>
#include <libcamera/framebuffer_allocator.h>
#include <libcamera/controls.h>
#include "camlib.h" // For CamFrame, CamConfig definitions

// Global CameraManager singleton (libcamera only allows ONE per process)
class GlobalCameraManager {
public:
    static GlobalCameraManager& instance();
    libcamera::CameraManager& get();

    // Prevent copies
    GlobalCameraManager(const GlobalCameraManager&) = delete;
    GlobalCameraManager& operator=(const GlobalCameraManager&) = delete;

private:
    GlobalCameraManager();
    ~GlobalCameraManager();

    libcamera::CameraManager cm_;
    std::mutex mutex_;
    bool started_;
};

class CameraLib {
public:
    CameraLib();
    ~CameraLib();

    bool init(int camera_index = 0);
    bool configure(const CamConfig& config);
    bool start();
    void stop();

    // Récupère une frame. Remplit le struct frame.
    // Retourne true si succès.
    bool getFrame(CamFrame* frame);

    // Legacy method
    bool takePhoto(const std::string &filename, int width = 640, int height = 480);

private:
    void requestComplete(libcamera::Request *request);

    // Use shared global CameraManager instead of per-instance
    std::shared_ptr<libcamera::Camera> camera_;
    std::unique_ptr<libcamera::FrameBufferAllocator> allocator_;

    // Stream configuration
    std::unique_ptr<libcamera::CameraConfiguration> config_;
    libcamera::Stream *stream_;

    // Frame synchronization
    std::atomic<bool> frame_ready_;
    std::mutex mutex_;
    libcamera::Request* completed_request_;

    // Buffer interne pour assembler les plans (Y+U+V) si nécessaire
    std::vector<uint8_t> frame_buffer_;
    bool started_;
    std::vector<uint8_t> jpeg_frame_;   // JPEG encodé
    int32_t target_fps_;                // Target framerate
};
