package fr.ensma.a3.ia.simuservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ensma.a3.ia.business.api.IMotionService;
import fr.ensma.a3.ia.business.service.MotionService;
import fr.ensma.a3.ia.simucontrollerbusiness.ISimuControllerBusiness;
import fr.ensma.a3.ia.simucontrollerbusiness.SimuControllerBusiness;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
@Command(name = "SimuServiceLauncher", description = "SimuServiceLauncher gere la partie simulateur.", mixinStandardHelpOptions = true)
public class SimuServiceLauncher implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SimuServiceLauncher.class);

    @Option(names = "--no-controller", description = "Desactive le module d'envoi des controles")
    boolean noController;

    @Option(names = "--no-sensor", description = "Desactive le module de reception des capteurs")
    boolean noSensor;

    @Option(names = "--no-dbox", description = "Desactive la connexion DBOX (mode test)")
    boolean noDbox;

    @Override
    public void run() {
        printBanner();

        boolean controllerEnabled = !noController;
        boolean sensorEnabled = !noSensor;
        boolean dboxEnabled = !noDbox;

        logger.info("Configuration: Controller={}, Sensors={}, DBOX={}",
            onOff(controllerEnabled), onOff(sensorEnabled), onOff(dboxEnabled));

        if (controllerEnabled) {
            ISimuControllerBusiness simuControllerBusiness = SimuControllerBusiness.getInstance();
            ControllerSenderService controllerSenderService = new ControllerSenderService(simuControllerBusiness);
            controllerSenderService.connect("wss://rcsimu-ia.ensma.fr/rccontroller/sender");
        }

        if (sensorEnabled) {
            IMotionService motionService = new MotionService(dboxEnabled);
            motionService.start();
            SensorFlowReceiverService sensorFlowInputService = new SensorFlowReceiverService(motionService);
            sensorFlowInputService.connect("wss://rcsimu-ia.ensma.fr/sensorflow/receiver");
        }
    }

    private String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    public static void main(String[] args) {
        new CommandLine(new SimuServiceLauncher()).execute(args);
    }

    public void printBanner() {
        System.out.println(
                """
                           _____ _                  _____                 _          _                            _
                          / ____(_)                / ____|               (_)        | |                          | |
                         | (___  _ _ __ ___  _   _| (___   ___ _ ____   ___  ___ ___| |     __ _ _   _ _ __   ___| |__   ___ _ __
                          \\___ \\| | '_ ` _ \\| | | |\\___ \\ / _ \\ '__\\ \\ / / |/ __/ _ \\ |    / _` | | | | '_ \\ / __| '_ \\ / _ \\ '__|
                          ____) | | | | | | | |_| |____) |  __/ |   \\ V /| | (_|  __/ |___| (_| | |_| | | | | (__| | | |  __/ |
                         |_____/|_|_| |_| |_|\\__,_|_____/ \\___|_|    \\_/ |_|\\___\\___|______\\__,_|\\__,_|_| |_|\\___|_| |_|\\___|_|

                    """);
    }
}
