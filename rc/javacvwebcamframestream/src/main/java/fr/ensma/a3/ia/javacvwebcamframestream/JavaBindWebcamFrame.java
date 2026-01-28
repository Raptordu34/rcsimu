package fr.ensma.a3.ia.javacvwebcamframestream;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class JavaBindWebcamFrame implements IJavaCVWebcamFrameStreamAPI {

    // Per-instance handles for multi-camera support
    private MethodHandle mhInit;
    private MethodHandle mhClose;
    private MethodHandle mhFrame;

    private Arena arena;
    private SymbolLookup lookup;
    private int cameraIndex = -1;

    // Shared library loading flag
    private static volatile boolean libLoaded = false;
    private static final Object libLoadLock = new Object();

    //pour rpi5 (pas la meme que sur x64..)
    static final MemoryLayout CAM_FRAME_LAYOUT = MemoryLayout.structLayout(
        ValueLayout.ADDRESS.withName("data"),
        ValueLayout.JAVA_INT.withName("size"),
        MemoryLayout.paddingLayout(4),                 // 12 → 16
        ValueLayout.JAVA_LONG.withName("timestamp_us"),// 16
        ValueLayout.JAVA_INT.withName("width"),        // 24
        ValueLayout.JAVA_INT.withName("height"),       // 28
        ValueLayout.JAVA_INT.withName("format"),       // 32
        MemoryLayout.paddingLayout(4)
    );

    @Override
    public boolean initializeStream(int deviceNumber) {
        this.cameraIndex = deviceNumber;

        // Load native library once (thread-safe)
        synchronized (libLoadLock) {
            if (!libLoaded) {
                String libPathStr = new java.io.File("target/native/libcam.so").getAbsolutePath();
                System.load(libPathStr);
                libLoaded = true;
            }
        }

        Linker linker = Linker.nativeLinker();

        // Use ofShared() for multi-thread access (each camera runs in its own thread)
        arena = Arena.ofShared();

        String libPathStr = new java.io.File("target/native/libcam.so").getAbsolutePath();
        lookup = SymbolLookup.libraryLookup(Path.of(libPathStr), arena);

        try {
            // Use _ex functions that support camera index
            mhInit = linker.downcallHandle(
                    lookup.find("init_camera_ex").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
            );

            mhFrame = linker.downcallHandle(
                    lookup.find("get_frame_ex").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );

            mhClose = linker.downcallHandle(
                    lookup.find("close_camera_ex").orElseThrow(),
                    FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)
            );

            int res = (int) mhInit.invoke(cameraIndex);
            return res == 0; // init_camera_ex returns 0 on success

        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public ByteBuffer getFrame() {
        if (cameraIndex < 0) return null;

        try {
            MemorySegment framePtr = (MemorySegment) mhFrame.invoke(cameraIndex);
            if (framePtr == null || framePtr.equals(MemorySegment.NULL)) return null;

            // On reinterpret la struct entière
            MemorySegment frame = framePtr.reinterpret(CAM_FRAME_LAYOUT.byteSize());

            // Récupère le pointeur vers data
            MemorySegment dataPtr =
                    frame.get(ValueLayout.ADDRESS, CAM_FRAME_LAYOUT.byteOffset(
                            MemoryLayout.PathElement.groupElement("data")));

            // Récupère la taille
            int size =
                    frame.get(ValueLayout.JAVA_INT, CAM_FRAME_LAYOUT.byteOffset(
                            MemoryLayout.PathElement.groupElement("size")));

            if (size <= 0) return null;

            // Redimensionner le segment pour correspondre à la taille du tableau uint8_t
            MemorySegment dataSegment = dataPtr.reinterpret(size);

            // Créer un ByteBuffer exploitable en Java
            return dataSegment.asByteBuffer();

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void closeStream() {
        if (cameraIndex < 0) return;

        try {
            mhClose.invoke(cameraIndex);
            arena.close();
            cameraIndex = -1;
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("unable to invoke close cam " + cameraIndex);
        }
    }
}