package fr.ensma.a3.ia.rcservice;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ensma.a3.ia.sensorsbusiness.ISensorAggregator;
import fr.ensma.a3.ia.sensorsbusiness.SensorAggregator;
import fr.ensma.a3.ia.webcamframestreambusiness.IWebcamFrameStream;
import fr.ensma.a3.ia.webcamframestreambusiness.WebcamFrameStream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
@Command(name = "RCServiceLauncher", description = "RCServiceLauncher gere la partie voiture telecommandee.", mixinStandardHelpOptions = true)
public class RCServiceLauncher implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(RCServiceLauncher.class);

  @Option(names = "--no-controller", description = "Desactive le module de reception des controles")
  boolean noController;

  @Option(names = "--no-sensor", description = "Desactive le module d'envoi des capteurs")
  boolean noSensor;

  @Option(names = "--no-webcamDriver", description = "Desactive le module d'envoi de la video pour le pilote")
  boolean noWebcamDriver;

  @Option(names = "--no-webcamAssistant", description = "Desactive le module d'envoi de la video pour l'assistant")
  boolean noWebcamAssistant;

  @Option(names = { "-u", "--url" }, description = "URL du serveur WebSocket", defaultValue = "wss://rcsimu-ia.ensma.fr")
  String wsUrl;

  @Override
  public void run() {
    printBanner();

    boolean controllerEnabled = !noController;
    boolean sensorEnabled = !noSensor;
    boolean webcamDriverEnabled = !noWebcamDriver;
    boolean webcamAssistantEnabled = !noWebcamAssistant;

    logger.info("Configuration: Controller={}, Sensors={}, WebcamDriver={}, WebcamAssistant={}",
        onOff(controllerEnabled), onOff(sensorEnabled), onOff(webcamDriverEnabled), onOff(webcamAssistantEnabled));

    if (controllerEnabled) {
      RCControllerReceiverService rcrs = new RCControllerReceiverService();
      rcrs.connect(wsUrl + "/rccontroller/receiver");
    }

    if (sensorEnabled) {
      try {
        ISensorAggregator sensorAggregator = new SensorAggregator();
        SensorFlowSenderService sfs = new SensorFlowSenderService(sensorAggregator);
        sfs.connect(wsUrl + "/sensorflow/sender");
      } catch (IOException e) {
        logger.error("Impossible d'initialiser les capteurs", e);
      }
    }

    IWebcamFrameStream frameStream = WebcamFrameStream.getInstance();
    if (webcamDriverEnabled) {
      WebcamDriverFrameStreamSenderService wDS = new WebcamDriverFrameStreamSenderService(frameStream);
      wDS.connect(wsUrl + "/webcamdriverstream/sender");
    }

    if (webcamAssistantEnabled) {
      WebcamAssistantFrameStreamSenderService wAS = new WebcamAssistantFrameStreamSenderService(frameStream);
      wAS.connect(wsUrl + "/webcamassistantstream/sender");
    }
  }

  private String onOff(boolean value) {
    return value ? "ON" : "OFF";
  }

  public void printBanner() {
    System.out.println("""

          _____   _____  _____                 _          _                            _
         |  __ \\ / ____|/ ____|               (_)        | |                          | |
         | |__) | |    | (___   ___ _ ____   ___  ___ ___| |     __ _ _   _ _ __   ___| |__   ___ _ __
         |  _  /| |     \\___ \\ / _ \\ '__\\ \\ / / |/ __/ _ \\ |    / _` | | | | '_ \\ / __| '_ \\ / _ \\ '__|
         | | \\ \\| |____ ____) |  __/ |   \\ V /| | (_|  __/ |___| (_| | |_| | | | | (__| | | |  __/ |
         |_|  \\_\\\\_____|_____/ \\___|_|    \\_/ |_|\\___\\___|______\\__,_|\\__,_|_| |_|\\___|_| |_|\\___|_|

        """);
  }

  public static void main(String[] args) {
    Config config = new Config();

    if (!config.isBootEnabled()) {
      logger.info("Boot desactive, arret");
      System.exit(0);
    }

    logger.info("Demarrage du service RC");

    if (config.isEthernet()) {
      try {
        logger.info("Initialisation module Waveshare Ethernet...");
        ProcessBuilder pb = new ProcessBuilder("sudo", "/usr/local/bin/waveshare-CM");
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
          logger.info("Module Waveshare initialise");
        } else {
          logger.warn("Waveshare retourne code {}", exitCode);
        }
        Thread.sleep(5000);
      } catch (IOException | InterruptedException e) {
        logger.error("Impossible de lancer waveshare-CM", e);
      }
    }

    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      logger.warn("Demarrage interrompu");
      Thread.currentThread().interrupt();
    }

    new CommandLine(new RCServiceLauncher()).execute(args);
  }
}
