package fr.ensma.a3.ia.simu.driver;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class NativeBE {

    // --- DLL location discovery ----
    static Path findDll() {
        Path cwd = Paths.get("").toAbsolutePath();

        Path[] candidates = new Path[]{
                cwd.resolve("lib/sim_input.dll"),
                cwd.resolve("sim_input.dll"),
                cwd.resolve("build/bin/Debug/sim_input.dll"),
                cwd.resolve("build/bin/Release/sim_input.dll"),
                cwd.resolve("../windowsinputapicontroller/build/bin/Debug/sim_input.dll"),
                cwd.resolve("../windowsinputapicontroller/build/bin/Release/sim_input.dll"),
                cwd.resolve("windowsinputapicontroller/build/bin/Debug/sim_input.dll"),
                cwd.resolve("windowsinputapicontroller/build/bin/Release/sim_input.dll")
        };

        for (Path p : candidates) {
            if (Files.exists(p)) return p.normalize();
        }

        throw new IllegalStateException("sim_input.dll not found. Looked in lib/, root, and build/bin/{Debug,Release}");
    }

    // ----- C struct layout (MUST match mapping.h) -----
    private static final MemoryLayout STATE_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("version"),
            ValueLayout.JAVA_BYTE.withName("connected"),
            MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_BYTE).withName("pad0"), // Explicit padding to match C++ _pad0[3]

            ValueLayout.JAVA_FLOAT.withName("lx"),
            ValueLayout.JAVA_FLOAT.withName("ly"),
            ValueLayout.JAVA_FLOAT.withName("rx"),
            ValueLayout.JAVA_FLOAT.withName("ry"),
            ValueLayout.JAVA_FLOAT.withName("lt"),
            ValueLayout.JAVA_FLOAT.withName("rt"),

            ValueLayout.JAVA_INT.withName("buttons"),

            ValueLayout.JAVA_SHORT.withName("di_lX"),
            ValueLayout.JAVA_SHORT.withName("di_lY"),

            ValueLayout.JAVA_SHORT.withName("di_lZ"),
            ValueLayout.JAVA_SHORT.withName("di_lRz"),
            ValueLayout.JAVA_SHORT.withName("di_s0"),
            ValueLayout.JAVA_SHORT.withName("di_s1"),

            MemoryLayout.sequenceLayout(32, ValueLayout.JAVA_BYTE).withName("di_buttons")
    );

    // ----- VarHandles -----
    private static final VarHandle VH_VERSION
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("version"));
    private static final VarHandle VH_CONNECTED
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("connected"));
    private static final VarHandle VH_LX
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lx"));
    private static final VarHandle VH_LY
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("ly"));
    private static final VarHandle VH_RX
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("rx"));
    private static final VarHandle VH_RY
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("ry"));
    private static final VarHandle VH_LT
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lt"));
    private static final VarHandle VH_RT
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("rt"));
    private static final VarHandle VH_BUTTONS
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("buttons"));

    private static final VarHandle VH_DI_LX
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("di_lX"));
    private static final VarHandle VH_DI_LY
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("di_lY"));

    private static final VarHandle VH_DI_LZ
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("di_lZ"));
    private static final VarHandle VH_DI_LRZ
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("di_lRz"));
    private static final VarHandle VH_DI_S0
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("di_s0"));
    private static final VarHandle VH_DI_S1
            = STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("di_s1"));

    public static final class State {
        public int version;
        public boolean connected;

        public float lx, ly, rx, ry, lt, rt;
        public int buttons;

        public short di_lX, di_lY;
        public short di_lZ, di_lRz, di_s0, di_s1;
    }

    public static final class Handle implements AutoCloseable {

        private final Arena arena = Arena.ofShared();
        private final MemorySegment stateSeg = arena.allocate(STATE_LAYOUT);
        private final int stateSize = (int) STATE_LAYOUT.byteSize();

        private volatile boolean closed = false;

        private final MethodHandle mhInit;
        private final MethodHandle mhPoll;
        private final MethodHandle mhShutdown;
        private final MethodHandle mhSetVibration; // XInput rumble

        private Handle(MethodHandle mhInit, MethodHandle mhPoll, MethodHandle mhShutdown, MethodHandle mhSetVibration) {
            this.mhInit = mhInit;
            this.mhPoll = mhPoll;
            this.mhShutdown = mhShutdown;
            this.mhSetVibration = mhSetVibration;

            VH_VERSION.set(stateSeg, 0L, 1);
        }

        public int init() throws Throwable {
            return (int) mhInit.invokeExact();
        }

        /** returns BE_PollState return code */
        public int pollRaw() throws Throwable {
            return (int) mhPoll.invokeExact(stateSeg, stateSize);
        }

        /** XInput vibration: userIndex (0..3), motors [0..1] */
        public int setXInputVibration(int userIndex, float leftMotor01, float rightMotor01) throws Throwable {
            return (int) mhSetVibration.invokeExact(userIndex, leftMotor01, rightMotor01);
        }

        public State readState() {
            State s = new State();

            s.version = (int) VH_VERSION.get(stateSeg, 0L);
            s.connected = ((byte) VH_CONNECTED.get(stateSeg, 0L)) != 0;

            s.lx = (float) VH_LX.get(stateSeg, 0L);
            s.ly = (float) VH_LY.get(stateSeg, 0L);
            s.rx = (float) VH_RX.get(stateSeg, 0L);
            s.ry = (float) VH_RY.get(stateSeg, 0L);
            s.lt = (float) VH_LT.get(stateSeg, 0L);
            s.rt = (float) VH_RT.get(stateSeg, 0L);

            s.buttons = (int) VH_BUTTONS.get(stateSeg, 0L);

            s.di_lX = (short) VH_DI_LX.get(stateSeg, 0L);
            s.di_lY = (short) VH_DI_LY.get(stateSeg, 0L);

            s.di_lZ = (short) VH_DI_LZ.get(stateSeg, 0L);
            s.di_lRz = (short) VH_DI_LRZ.get(stateSeg, 0L);
            s.di_s0 = (short) VH_DI_S0.get(stateSeg, 0L);
            s.di_s1 = (short) VH_DI_S1.get(stateSeg, 0L);

            return s;
        }

        @Override
        public synchronized void close() {
            if (closed) return;
            closed = true;

            try {
                // garante parar vibração ao fechar
                mhSetVibration.invokeExact(0, 0f, 0f);
            } catch (Throwable ignored) {
            }

            try {
                mhShutdown.invokeExact();
            } catch (Throwable ignored) {
            }

            arena.close();
        }
    }

    public static Handle open(String dllPath) throws Throwable {
        if (dllPath == null || dllPath.isBlank()) {
            dllPath = findDll().toString();
        }

        System.load(dllPath);

        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = SymbolLookup.loaderLookup();

        MethodHandle mhInit = linker.downcallHandle(
                lookup.find("BE_Init").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT)
        ).asType(MethodType.methodType(int.class));

        MethodHandle mhPoll = linker.downcallHandle(
                lookup.find("BE_PollState").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        ).asType(MethodType.methodType(int.class, MemorySegment.class, int.class));

        MethodHandle mhShutdown = linker.downcallHandle(
                lookup.find("BE_Shutdown").orElseThrow(),
                FunctionDescriptor.ofVoid()
        ).asType(MethodType.methodType(void.class));

        // ===== VIBRATION =====
        MethodHandle mhSetVibration;
        var vibOpt = lookup.find("be_set_xinput_vibration");
        if (vibOpt.isPresent()) {
            mhSetVibration = linker.downcallHandle(
                    vibOpt.get(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT,
                            ValueLayout.JAVA_INT,
                            ValueLayout.JAVA_FLOAT,
                            ValueLayout.JAVA_FLOAT
                    )
            ).asType(MethodType.methodType(int.class, int.class, float.class, float.class));
        } else {
            // fallback: se DLL não tiver o símbolo, não quebra
            mhSetVibration = MethodHandles.constant(int.class, 0)
                    .asType(MethodType.methodType(int.class, int.class, float.class, float.class));
        }

        return new Handle(mhInit, mhPoll, mhShutdown, mhSetVibration);
    }

    private NativeBE() {}
}
