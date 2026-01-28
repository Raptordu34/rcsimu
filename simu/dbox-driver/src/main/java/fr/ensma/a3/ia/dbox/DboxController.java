package fr.ensma.a3.ia.dbox;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API Java pour controler la plateforme DBOX via Panama FFM.
 *
 * Cette classe encapsule les appels natifs vers SimpleDboxController (C++)
 * via le wrapper dbox_controller_wrapper.dll et l'API Panama Foreign Function & Memory.
 *
 * <h2>Architecture</h2>
 * <pre>
 *   DboxController.java (cette classe)
 *       |
 *       | Panama FFM (MethodHandle)
 *       v
 *   dbox_controller_wrapper.dll
 *       |
 *       | Appels C++
 *       v
 *   SimpleDboxAPI.dll
 *       |
 *       v
 *   dbxLive64.dll (SDK DBOX officiel)
 * </pre>
 *
 * <h2>Exemple d'utilisation</h2>
 * <pre>{@code
 *   try (DboxController dbox = new DboxController()) {
 *       if (!dbox.connect()) {
 *           System.err.println("Erreur: " + dbox.getLastError());
 *           return;
 *       }
 *
 *       dbox.start();
 *
 *       // Boucle de controle a 100Hz
 *       while (running) {
 *           dbox.update(roll, pitch, heave, rpm, torque);
 *           Thread.sleep(10);
 *       }
 *
 *       dbox.stop();
 *   } // disconnect() appele automatiquement
 * }</pre>
 *
 * <h2>Prerequis</h2>
 * <ul>
 *   <li>Java 21+ (pour Panama FFM)</li>
 *   <li>DLLs dans le classpath ou java.library.path :
 *       dbox_controller_wrapper.dll, SimpleDboxAPI.dll, dbxLive64.dll</li>
 *   <li>DBOX Control Panel lance et hardware connecte</li>
 * </ul>
 *
 * @author Migration UDP -> FFM
 * @since 2024
 */
public class DboxController implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(DboxController.class);

    // =========================================================================
    // CONSTANTES
    // =========================================================================

    /** Nom de la DLL du wrapper */
    private static final String WRAPPER_DLL = "dbox_controller_wrapper";

    /** Nom de la DLL SimpleDboxAPI */
    private static final String SIMPLE_API_DLL = "SimpleDboxAPI";

    /** Nom de la DLL SDK DBOX */
    private static final String DBOX_SDK_DLL = "dbxLive64";

    /** Chemins de recherche pour les DLLs (relatifs au working directory) */
    private static final String[] DLL_SEARCH_PATHS = {
        "lib",
        "",  // Working directory
        "dbox-driver\\lib"
    };

    // =========================================================================
    // HANDLES NATIFS (MethodHandle pour chaque fonction C)
    // =========================================================================

    // Cycle de vie
    private static MethodHandle h_Create;
    private static MethodHandle h_Destroy;
    private static MethodHandle h_Connect;
    private static MethodHandle h_Disconnect;
    private static MethodHandle h_Start;
    private static MethodHandle h_Stop;

    // Controle des mouvements
    private static MethodHandle h_Update;
    private static MethodHandle h_SetMotion;
    private static MethodHandle h_SetVibration;
    private static MethodHandle h_ResetToNeutral;

    // Etat
    private static MethodHandle h_IsConnected;
    private static MethodHandle h_IsRunning;
    private static MethodHandle h_GetLastError;

    // Configuration
    private static MethodHandle h_SetMasterGain;
    private static MethodHandle h_SetEngineRange;
    private static MethodHandle h_SetMaxTorque;

    // =========================================================================
    // ETAT DE L'INSTANCE
    // =========================================================================

    /** Pointeur natif vers SimpleDboxController (opaque handle) */
    private MemorySegment nativeHandle;

    /** Arena pour la gestion memoire de cette instance */
    private Arena arena;

    /** Indicateur si l'instance a ete fermee */
    private boolean closed = false;

    /** Indicateur si les DLLs ont ete chargees */
    private static boolean librariesLoaded = false;

    /** Message d'erreur du chargement des DLLs */
    private static String loadError = null;

    // =========================================================================
    // INITIALISATION STATIQUE (chargement des DLLs)
    // =========================================================================

    static {
        try {
            loadNativeLibraries();
            initializeMethodHandles();
            librariesLoaded = true;
            logger.info("Native libraries loaded successfully");
        } catch (Throwable t) {
            loadError = t.getMessage();
            logger.error("Failed to load native libraries: {}", loadError, t);
        }
    }

    /**
     * Charge les DLLs natives dans l'ordre correct.
     */
    private static void loadNativeLibraries() {
        logger.debug("Loading native libraries...");

        // Ordre important : charger les dependances avant le wrapper
        // 1. dbxLive64.dll (SDK DBOX - pas de dependances)
        // 2. SimpleDboxAPI.dll (depend de dbxLive64)
        // 3. dbox_controller_wrapper.dll (depend de SimpleDboxAPI)

        String[] dllsToLoad = { DBOX_SDK_DLL, SIMPLE_API_DLL, WRAPPER_DLL };

        for (String dllName : dllsToLoad) {
            loadDll(dllName);
        }
    }

    /**
     * Charge une DLL en cherchant dans plusieurs emplacements.
     */
    private static void loadDll(String dllName) {
        // 1. Chercher d'abord dans les emplacements locaux prioritaires (lib, etc.)
        String workingDir = System.getProperty("user.dir");

        for (String searchPath : DLL_SEARCH_PATHS) {
            String fullPath;
            if (searchPath.isEmpty()) {
                fullPath = workingDir + "\\" + dllName + ".dll";
            } else {
                fullPath = workingDir + "\\" + searchPath + "\\" + dllName + ".dll";
            }

            java.io.File file = new java.io.File(fullPath);
            if (file.exists()) {
                try {
                    System.load(fullPath);
                    logger.debug("{} loaded from {}", dllName, fullPath);
                    return;
                } catch (UnsatisfiedLinkError e) {
                    logger.warn("Found {} at {} but failed to load: {}", dllName, fullPath, e.getMessage());
                }
            }
        }

        // 2. En dernier recours, essayer via loadLibrary (java.library.path / PATH systeme)
        try {
            System.loadLibrary(dllName);
            logger.debug("{} loaded via loadLibrary", dllName);
            return;
        } catch (UnsatisfiedLinkError ignored) {
            // Aucun emplacement n'a fonctionne
        }

        throw new UnsatisfiedLinkError(
            "Could not find " + dllName + ".dll in any of: known search paths (lib/, etc.) or java.library.path"
        );
    }

    /**
     * Initialise les MethodHandles pour toutes les fonctions C exportees.
     */
    private static void initializeMethodHandles() {
        logger.debug("Initializing method handles...");

        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = SymbolLookup.loaderLookup();

        // --- Cycle de vie ---

        h_Create = linker.downcallHandle(
            lookup.find("DBOX_Controller_Create").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_Create")),
            FunctionDescriptor.of(ValueLayout.ADDRESS)
        );

        h_Destroy = linker.downcallHandle(
            lookup.find("DBOX_Controller_Destroy").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_Destroy")),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        h_Connect = linker.downcallHandle(
            lookup.find("DBOX_Controller_Connect").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_Connect")),
            FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS)
        );

        h_Disconnect = linker.downcallHandle(
            lookup.find("DBOX_Controller_Disconnect").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_Disconnect")),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        h_Start = linker.downcallHandle(
            lookup.find("DBOX_Controller_Start").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_Start")),
            FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS)
        );

        h_Stop = linker.downcallHandle(
            lookup.find("DBOX_Controller_Stop").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_Stop")),
            FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS)
        );

        // --- Controle des mouvements ---

        h_Update = linker.downcallHandle(
            lookup.find("DBOX_Controller_Update").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_Update")),
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_FLOAT,
                ValueLayout.JAVA_FLOAT,
                ValueLayout.JAVA_FLOAT,
                ValueLayout.JAVA_FLOAT,
                ValueLayout.JAVA_FLOAT
            )
        );

        h_SetMotion = linker.downcallHandle(
            lookup.find("DBOX_Controller_SetMotion").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_SetMotion")),
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_FLOAT,
                ValueLayout.JAVA_FLOAT,
                ValueLayout.JAVA_FLOAT
            )
        );

        h_SetVibration = linker.downcallHandle(
            lookup.find("DBOX_Controller_SetVibration").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_SetVibration")),
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_FLOAT,
                ValueLayout.JAVA_FLOAT
            )
        );

        h_ResetToNeutral = linker.downcallHandle(
            lookup.find("DBOX_Controller_ResetToNeutral").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_ResetToNeutral")),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        // --- Etat ---

        h_IsConnected = linker.downcallHandle(
            lookup.find("DBOX_Controller_IsConnected").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_IsConnected")),
            FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS)
        );

        h_IsRunning = linker.downcallHandle(
            lookup.find("DBOX_Controller_IsRunning").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_IsRunning")),
            FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS)
        );

        h_GetLastError = linker.downcallHandle(
            lookup.find("DBOX_Controller_GetLastError").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_GetLastError")),
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );

        // --- Configuration ---

        h_SetMasterGain = linker.downcallHandle(
            lookup.find("DBOX_Controller_SetMasterGain").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_SetMasterGain")),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_FLOAT)
        );

        h_SetEngineRange = linker.downcallHandle(
            lookup.find("DBOX_Controller_SetEngineRange").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_SetEngineRange")),
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_FLOAT,
                ValueLayout.JAVA_FLOAT
            )
        );

        h_SetMaxTorque = linker.downcallHandle(
            lookup.find("DBOX_Controller_SetMaxTorque").orElseThrow(
                () -> new RuntimeException("Symbol not found: DBOX_Controller_SetMaxTorque")),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_FLOAT)
        );

        logger.debug("All method handles initialized");
    }

    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================

    /**
     * Cree une nouvelle instance du controleur DBOX.
     *
     * @throws IllegalStateException si les DLLs n'ont pas pu etre chargees
     * @throws RuntimeException si la creation du controleur natif echoue
     */
    public DboxController() {
        if (!librariesLoaded) {
            throw new IllegalStateException(
                "Native libraries not loaded: " + loadError);
        }

        try {
            this.arena = Arena.ofConfined();
            this.nativeHandle = (MemorySegment) h_Create.invoke();

            if (this.nativeHandle == null || this.nativeHandle.address() == 0) {
                throw new RuntimeException("DBOX_Controller_Create returned NULL");
            }

            logger.debug("Controller created at 0x{}", Long.toHexString(nativeHandle.address()));

        } catch (Throwable t) {
            if (arena != null) {
                arena.close();
            }
            throw new RuntimeException("Failed to create DboxController", t);
        }
    }

    // =========================================================================
    // CYCLE DE VIE
    // =========================================================================

    /**
     * Connecte et initialise la plateforme DBOX.
     * @return true si la connexion a reussi, false sinon
     */
    public boolean connect() {
        checkNotClosed();
        try {
            return (boolean) h_Connect.invoke(nativeHandle);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling connect()", t);
        }
    }

    /**
     * Deconnecte et libere les ressources DBOX.
     */
    public void disconnect() {
        if (closed || nativeHandle == null) {
            return;
        }
        try {
            h_Disconnect.invoke(nativeHandle);
        } catch (Throwable t) {
            logger.error("Error in disconnect", t);
        }
    }

    /**
     * Demarre la simulation avec fade-in progressif.
     * @return true si le demarrage a reussi
     */
    public boolean start() {
        checkNotClosed();
        try {
            return (boolean) h_Start.invoke(nativeHandle);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling start()", t);
        }
    }

    /**
     * Arrete la simulation avec fade-out progressif.
     * @return true si l'arret a reussi
     */
    public boolean stop() {
        checkNotClosed();
        try {
            return (boolean) h_Stop.invoke(nativeHandle);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling stop()", t);
        }
    }

    // =========================================================================
    // CONTROLE DES MOUVEMENTS
    // =========================================================================

    /**
     * Met a jour tous les parametres de mouvement.
     * @param roll   Roulis [-1.0, +1.0]
     * @param pitch  Tangage [-1.0, +1.0]
     * @param heave  Pilonnement [-1.0, +1.0]
     * @param rpm    Regime moteur [0, 10000]
     * @param torque Couple moteur [0, 1000]
     */
    public void update(float roll, float pitch, float heave, float rpm, float torque) {
        checkNotClosed();
        try {
            h_Update.invoke(nativeHandle, roll, pitch, heave, rpm, torque);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling update()", t);
        }
    }

    /**
     * Definit uniquement les mouvements (roll, pitch, heave).
     */
    public void setMotion(float roll, float pitch, float heave) {
        checkNotClosed();
        try {
            h_SetMotion.invoke(nativeHandle, roll, pitch, heave);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling setMotion()", t);
        }
    }

    /**
     * Definit uniquement les vibrations moteur.
     */
    public void setVibration(float rpm, float torque) {
        checkNotClosed();
        try {
            h_SetVibration.invoke(nativeHandle, rpm, torque);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling setVibration()", t);
        }
    }

    /**
     * Retourne immediatement a la position neutre.
     */
    public void resetToNeutral() {
        checkNotClosed();
        try {
            h_ResetToNeutral.invoke(nativeHandle);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling resetToNeutral()", t);
        }
    }

    // =========================================================================
    // INTERROGATION D'ETAT
    // =========================================================================

    public boolean isConnected() {
        if (closed || nativeHandle == null) {
            return false;
        }
        try {
            return (boolean) h_IsConnected.invoke(nativeHandle);
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isRunning() {
        if (closed || nativeHandle == null) {
            return false;
        }
        try {
            return (boolean) h_IsRunning.invoke(nativeHandle);
        } catch (Throwable t) {
            return false;
        }
    }

    public String getLastError() {
        if (closed || nativeHandle == null) {
            return "Controller is closed";
        }
        try {
            MemorySegment errorPtr = (MemorySegment) h_GetLastError.invoke(nativeHandle);
            if (errorPtr == null || errorPtr.address() == 0) {
                return "";
            }
            // Reinterpret with arena scope and read the C string
            MemorySegment bounded = errorPtr.reinterpret(1024, arena, null);
            // Read null-terminated C string manually
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1024; i++) {
                byte b = bounded.get(ValueLayout.JAVA_BYTE, i);
                if (b == 0) break;
                sb.append((char) b);
            }
            return sb.toString();
        } catch (Throwable t) {
            return "Error getting last error: " + t.getMessage();
        }
    }

    // =========================================================================
    // CONFIGURATION AVANCEE
    // =========================================================================

    public void setMasterGain(float gainDb) {
        checkNotClosed();
        try {
            h_SetMasterGain.invoke(nativeHandle, gainDb);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling setMasterGain()", t);
        }
    }

    public void setEngineRange(float idleRpm, float maxRpm) {
        checkNotClosed();
        try {
            h_SetEngineRange.invoke(nativeHandle, idleRpm, maxRpm);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling setEngineRange()", t);
        }
    }

    public void setMaxTorque(float maxTorque) {
        checkNotClosed();
        try {
            h_SetMaxTorque.invoke(nativeHandle, maxTorque);
        } catch (Throwable t) {
            throw new RuntimeException("Error calling setMaxTorque()", t);
        }
    }

    // =========================================================================
    // AUTOCLOSEABLE
    // =========================================================================

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        try {
            if (isConnected()) {
                disconnect();
            }

            if (nativeHandle != null && nativeHandle.address() != 0) {
                h_Destroy.invoke(nativeHandle);
                logger.debug("Controller destroyed");
            }
        } catch (Throwable t) {
            logger.error("Error during close", t);
        } finally {
            if (arena != null) {
                arena.close();
            }
            nativeHandle = null;
        }
    }

    // =========================================================================
    // METHODES PRIVEES
    // =========================================================================

    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("DboxController is closed");
        }
        if (nativeHandle == null || nativeHandle.address() == 0) {
            throw new IllegalStateException("Native handle is invalid");
        }
    }

    // =========================================================================
    // METHODES STATIQUES UTILITAIRES
    // =========================================================================

    public static boolean isNativeLibraryLoaded() {
        return librariesLoaded;
    }

    public static String getNativeLibraryLoadError() {
        return loadError;
    }
}
