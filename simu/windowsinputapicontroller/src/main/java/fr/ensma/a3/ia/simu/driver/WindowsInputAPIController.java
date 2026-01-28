package fr.ensma.a3.ia.simu.driver;

public final class WindowsInputAPIController implements IWindowsInputAPIController {

    private final NativeBE.Handle be;

    private final AxisModel axis = new AxisModel();
    private final ButtonsModel buttons = new ButtonsModel();
    private boolean connected;

    private static final float DZ_STICK = 0.08f;
    private static final float DZ_TRIG = 0.02f;

    // Opcional: liga/desliga logs rápidos de debug
    private static final boolean DEBUG = true;
    private int dbgCounter = 0;

    // ===== Calibração DI pedais (repouso) =====
    private boolean diCalibrated = false;
    private short diY0 = 0;   // repouso acelerador (DI Y raw)
    private short diRz0 = 0;  // repouso freio (DI RZ raw)

    // sentido detectado automaticamente:
    // true = pressionar aumenta raw
    // false = pressionar diminui raw
    // null = ainda não detectado (reporta 0)
    private Boolean yPressIncreases = null;
    private Boolean rzPressIncreases = null;

    // ===== RUMBLE (XInput) =====
    private float rumbleLeft = 0f;
    private float rumbleRight = 0f;

    // limiar pequeno só pra evitar ruído na detecção
    private static final int DETECT_THRESHOLD = 300;

    public WindowsInputAPIController(NativeBE.Handle be) {
        this.be = be;
    }

    private static float deadzoneStick(float v) {
        return (Math.abs(v) < DZ_STICK) ? 0f : v;
    }

    private static float deadzoneTrig(float v) {
        return (v < DZ_TRIG) ? 0f : v;
    }

    /**
     * DirectInput short [-32768..32767] -> float [-1..1] (simétrico)
     */
    private static float normShortAxis(short v) {
        if (v >= 0) {
            return Math.min(1f, v / 32767f);
        }
        return Math.max(-1f, v / 32768f);
    }

    private static float clamp01(float v) {
        if (v < 0f) {
            return 0f;
        }
        if (v > 1f) {
            return 1f;
        }
        return v;
    }

    /**
     * Detecta o sentido do pedal comparando com o repouso: retorna true se
     * aumentou, false se diminuiu, null se ainda é ruído.
     */
    private static Boolean detectDirection(short raw, short raw0) {
        int d = raw - raw0;
        if (Math.abs(d) < DETECT_THRESHOLD) {
            return null;
        }
        return d > 0;
    }

    /**
     * Normaliza pedal DI para [0..1] usando: - repouso raw0 -> 0 - extremo do
     * eixo -> 1 Com auto-inversão via pressIncreases.
     */
    private static float pedal01(short raw, short raw0, Boolean pressIncreases) {
        if (pressIncreases == null) {
            return 0f;
        }

        if (pressIncreases) {
            // 0 em raw0, 1 em +32767
            float denom = (32767f - raw0);
            if (denom == 0f) {
                return 0f;
            }
            return clamp01((raw - raw0) / denom);
        } else {
            // 0 em raw0, 1 em -32768
            float denom = (raw0 - (-32768f));
            if (denom == 0f) {
                return 0f;
            }
            return clamp01((raw0 - raw) / denom);
        }
    }

    // ===== RUMBLE (XInput) =====
    private void setRumble(float left01, float right01) {
        try {
            be.setXInputVibration(0, clamp01(left01), clamp01(right01)); // userIndex 0
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void requestRumble(float left01, float right01) {
        this.rumbleLeft = clamp01(left01);
        this.rumbleRight = clamp01(right01);
    }

    private void stopRumble() {
        setRumble(0f, 0f);
    }

    @Override
    public boolean poll() {
        try {
            int ok = be.pollRaw();
            NativeBE.State st = be.readState();

            connected = (ok != 0) && st.connected;
            if (!connected) {
                if (DEBUG && (dbgCounter++ % 30 == 0)) {
                    System.out.println(
                            "[JAVA] poll: not connected / no data (ok=" + ok + ", st.connected=" + st.connected + ")");
                }

                axis.clear();
                buttons.clear();

                // para vibração ao desconectar
                stopRumble();

                // se desconectar, recalibra ao reconectar
                diCalibrated = false;
                yPressIncreases = null;
                rzPressIncreases = null;

                return false;
            }

            // Calibra no primeiro poll conectado (pedais soltos!)
            if (!diCalibrated) {
                diY0 = st.di_lY;
                diRz0 = st.di_lRz;
                diCalibrated = true;

                yPressIncreases = null;
                rzPressIncreases = null;

                if (DEBUG) {
                    System.out.println("[JAVA] DI calibrated: diY0=" + diY0 + " diRz0=" + diRz0);
                }
            }

            // Detecta o sentido automaticamente na primeira mexida
            if (yPressIncreases == null) {
                yPressIncreases = detectDirection(st.di_lY, diY0);
            }
            if (rzPressIncreases == null) {
                rzPressIncreases = detectDirection(st.di_lRz, diRz0);
            }

            axis.set(
                    // XInput / eixos centrados
                    deadzoneStick(st.lx),
                    deadzoneStick(st.ly),
                    deadzoneStick(st.rx),
                    deadzoneStick(st.ry),
                    // triggers XInput
                    deadzoneTrig(st.lt),
                    deadzoneTrig(st.rt),
                    // DirectInput axes [-1..1]
                    deadzoneStick(normShortAxis(st.di_lX)),
                    deadzoneStick(normShortAxis(st.di_lY)),
                    deadzoneStick(normShortAxis(st.di_lZ)),
                    deadzoneStick(normShortAxis(st.di_lRz)),
                    // sliders: centrados
                    deadzoneStick(normShortAxis(st.di_s0)),
                    deadzoneStick(normShortAxis(st.di_s1))
            );

            buttons.setMask(st.buttons);

            setRumble(rumbleLeft, rumbleRight);

            if (DEBUG && (dbgCounter++ % 30 == 0)) {
                System.out.printf(
                        "[JAVA] ok=%d connected=%s%n" +
                        "  RAW: diX=%d diY=%d diZ=%d diRz=%d s0=%d s1=%d%n" +
                        "  NORM: lx=%.3f ly=%.3f rx=%.3f ry=%.3f lt=%.3f rt=%.3f%n" +
                        "  DI_NORM: diX=%.3f diY=%.3f diZ=%.3f diRz=%.3f s0=%.3f s1=%.3f%n",
                        ok, st.connected,
                        (int) st.di_lX, (int) st.di_lY, (int) st.di_lZ, (int) st.di_lRz, (int) st.di_s0, (int) st.di_s1,
                        axis.getLx(), axis.getLy(), axis.getRx(), axis.getRy(), axis.getLt(), axis.getRt(),
                        axis.getDix(), axis.getDiy(), axis.getDiz(), axis.getDiRz(), axis.getDiS0(), axis.getDiS1()
                );
            }

            return true;

        } catch (Throwable t) {
            if (DEBUG) {
                System.out.println("[JAVA] poll exception: " + t);
                t.printStackTrace();
            }
            connected = false;
            axis.clear();
            buttons.clear();

            // garante parar vibração se der exceção
            stopRumble();

            diCalibrated = false;
            yPressIncreases = null;
            rzPressIncreases = null;
            return false;
        }
    }

    @Override
    public AxisModel axis() {
        return axis;
    }

    @Override
    public ButtonsModel buttons() {
        return buttons;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void close() {
        try {
            stopRumble();
            be.close();
        } catch (Throwable ignored) {
        }
    }
}
