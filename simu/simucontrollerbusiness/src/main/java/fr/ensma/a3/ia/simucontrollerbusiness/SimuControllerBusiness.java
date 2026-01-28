package fr.ensma.a3.ia.simucontrollerbusiness;

import fr.ensma.a3.ia.simu.driver.AxisModel;
import fr.ensma.a3.ia.simu.driver.IWindowsInputAPIController;
import fr.ensma.a3.ia.simu.driver.NativeBE;
import fr.ensma.a3.ia.simu.driver.WindowsInputAPIController;

public final class SimuControllerBusiness implements ISimuControllerBusiness {

    private final IWindowsInputAPIController input;

    /* ========= SINGLETON ========= */
    private static volatile SimuControllerBusiness instance;
    private static NativeBE.Handle nativeHandle;
    private static IWindowsInputAPIController inputController;

    /* ========= PARAMÈTRES MÉTIER ========= */
    private static final float DEAD_ZONE = 0.05f;
    private static final float STEER_SENSITIVITY = 5.0f; // Multiplicateur de sensibilité du volant (>1 = plus sensible)
    private static final int BTN_RESET_CAMERA = 0;

    public SimuControllerBusiness(IWindowsInputAPIController input) {
        this.input = input;
    }

    /**
     * Retourne l'instance singleton de SimuControllerBusiness.
     * Initialise automatiquement la connexion au contrôleur Windows lors du premier appel.
     *
     * @return l'instance singleton
     * @throws RuntimeException si l'initialisation du contrôleur échoue
     */
    public static synchronized SimuControllerBusiness getInstance() {
        if (instance == null) {
            try {
                System.out.println("Initializing SimuControllerBusiness...");

                // Force le chemin de la DLL si défini via propriété système
                String forcedDll = System.getProperty("simu.native.dll");

                // Ouvre le handle natif vers la DLL
                nativeHandle = NativeBE.open(forcedDll);

                // NOTE: L'initialisation (BE_Init) est déplacée dans le thread de polling
                // pour garantir que la création des devices DirectInput et le polling
                // se font sur le même thread (Thread Affinity).

                // Crée le contrôleur Windows
                inputController = new WindowsInputAPIController(nativeHandle);

                // Crée l'instance business
                instance = new SimuControllerBusiness(inputController);

                // Thread pour polling continu du contrôleur
                Thread pollThread = new Thread(() -> {
                    try {
                        System.out.println("[POLL-THREAD] Starting controller polling thread...");

                        // Init sur le même thread que le polling
                        int rcInit = nativeHandle.init();
                        if (rcInit == 0) {
                            System.err.println("[POLL-THREAD] Failed to initialize Windows Input API (BE_Init returned 0)");
                            // On continue quand même, au cas où ça marcherait plus tard ou en mode dégradé
                        } else {
                            System.out.println("[POLL-THREAD] Windows Input API initialized (BE_Init rc = " + rcInit + ")");
                        }

                        // DIAGNOSTIC : Ajouter un délai initial pour laisser la DLL se stabiliser
                        System.out.println("[POLL-THREAD] Waiting 500ms before first poll...");
                        Thread.sleep(500);

                        boolean wasConnected = false;
                        int pollCount = 0;
                        int consecutiveFailures = 0;

                        while (!Thread.currentThread().isInterrupted()) {
                            pollCount++;
                            boolean connected = inputController.poll();

                            // DIAGNOSTIC : Logger les 10 premiers polls et les échecs consécutifs
                            if (pollCount <= 10 || consecutiveFailures > 0) {
                                System.out.printf("[POLL-THREAD] Poll #%d: connected=%b, isConnected()=%b%n",
                                    pollCount, connected, inputController.isConnected());
                            }

                            if (!connected) {
                                consecutiveFailures++;
                            } else {
                                if (consecutiveFailures > 0) {
                                    System.out.println("[POLL-THREAD] Recovered after " + consecutiveFailures + " failures");
                                }
                                consecutiveFailures = 0;
                            }

                            // N'affiche le message que lors d'un changement d'état
                            if (connected != wasConnected) {
                                if (connected) {
                                    System.out.println("[STATE-CHANGE] Controller connected (poll #" + pollCount + ")");
                                } else {
                                    System.err.println("[STATE-CHANGE] Controller disconnected (poll #" + pollCount + ")");
                                }
                                wasConnected = connected;
                            }

                            // Met à jour le rumble seulement si connecté
                            if (connected) {
                                instance.updateRumble();
                            }

                            Thread.sleep(100); // Poll à 10 Hz
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("[POLL-THREAD] Controller polling thread stopped");
                    } catch (Throwable t) {
                        System.err.println("[POLL-THREAD] Fatal error in polling thread:");
                        t.printStackTrace();
                    }
                }, "ControllerPollThread");

                pollThread.setDaemon(true);
                pollThread.start();

                System.out.println("SimuControllerBusiness initialized successfully");
                System.out.println("[INIT] Thread '" + pollThread.getName() + "' started (daemon=" + pollThread.isDaemon() + ")");

            } catch (Throwable t) {
                throw new RuntimeException("Failed to initialize SimuControllerBusiness", t);
            }
        }
        return instance;
    }

    /* ========= PILOTAGE ========= */
    @Override
    public float getSteer() {
        AxisModel a = input.axis();
        // Volant Simucube (di_lX) + Manette Xbox (lx)
        // Les deux sont centrés [-1..1]. On somme pour supporter l'un ou l'autre.
        float val = a.getDix() + a.getLx();
        // Appliquer le multiplicateur de sensibilité avant clamping
        val *= STEER_SENSITIVITY;
        return applyDeadZone(clamp11(val));
    }

    @Override
    public float getThrottle() {
        AxisModel a = input.axis();

        // 1. Pédales SC-Link Hub (di_s0 = raw lRx) [-1..1]
        float pedal = a.getDiS0();

        // 2. Gâchette Manette Xbox (rt) [0..1] → convertir en [-1..1]
        float trigger = a.getRt() * 2.0f - 1.0f;

        // Prendre le max des deux
        return applyDeadZone(Math.max(pedal, trigger));
    }

    @Override
    public float getBrake() {
        AxisModel a = input.axis();

        // 1. Pédales SC-Link Hub (di_s1 = raw lRy) [-1..1]
        float pedal = a.getDiS1();

        // 2. Gâchette Manette Xbox (lt) [0..1] → convertir en [-1..1]
        float trigger = a.getLt() * 2.0f - 1.0f;

        // Prendre le max des deux
        return applyDeadZone(Math.max(pedal, trigger));
    }

    /* ========= RUMBLE (MÉTIER, GÉNÉRIQUE) ========= */
    @Override
    public void updateRumble() {
        if (!input.isConnected()) {
            input.requestRumble(0f, 0f);
            return;
        }

        AxisModel a = input.axis();

        // mapping générique (utilisant les gâchettes pour le retour d'effort si dispo)
        float leftMotor = computeLeftVibration(a.getLt());
        float rightMotor = computeRightVibration(a.getRt());

        input.requestRumble(leftMotor, rightMotor);
    }

    private float computeRightVibration(float throttle) {
        return clamp01(throttle);
    }

    private float computeLeftVibration(float brake) {
        return clamp01(brake);
    }

    /* ========= CAMÉRA ========= */
    @Override
    public float getHorizontalPanAssistantCamera() {
        // Stick Droit Manette (rx)
        return applyDeadZone(input.axis().getRx());
    }

    @Override
    public float getVerticalPanAssistantCamera() {
        // Stick Droit Manette (ry)
        return applyDeadZone(input.axis().getRy());
    }

    @Override
    public boolean getInitPanAssistantCamera() {
        return input.buttons().isPressed(BTN_RESET_CAMERA);
    }

    /* ========= OUTILS ========= */
    private static float applyDeadZone(float v) {
        return Math.abs(v) < DEAD_ZONE ? 0f : v;
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    private static float clamp11(float v) {
        if (v < -1f) return -1f;
        if (v > 1f) return 1f;
        return v;
    }
}
