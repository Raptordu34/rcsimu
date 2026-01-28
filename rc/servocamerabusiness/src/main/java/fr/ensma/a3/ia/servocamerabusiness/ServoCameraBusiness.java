package fr.ensma.a3.ia.servocamerabusiness;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.ensma.a3.ia.servohatdriver.ServoControlDriver;


public class ServoCameraBusiness implements IServoCameraBusiness {
    int idPWMHoriz = 0;
    int idPWMVert = 1;
    int modeDeplacement = 0; // 0 : incrémentation, 1 : position absolue
    private final int maxAxe0 = 1900;
    private final int minAxe0 = 1100;
    private final int maxAxe1 = 1900;
    private final int minAxe1 = 1100;
    private final int centerAxe0 = 1500;
    private final int centerAxe1 = 1600;
    private int servo0Position = centerAxe0; // Position initiale du servo 0
    private int servo1Position = centerAxe1; // Position initiale du servo 1

    private ServoControlDriver pwmControler;

    // Initialisation par défault des servos
    public ServoCameraBusiness() throws IllegalArgumentException {
        pwmControler = ServoControlDriver.getSingleRef();
        pwmControler.setServoPulseCameraHor(servo0Position);
        pwmControler.setServoPulseCameraVer(servo1Position);
        logger.info("Création de la caméra servo avec les branchements de base : " + idPWMHoriz + " & " + idPWMVert);
    }

    /*
    Canal pwm a modif dans le servo_duty.c de servohatdriver native

    // Initialisation des servos avec les branchements physiques sur la carte PWM
    public ServoCameraBusiness(int idPWMHorizInput, int idPWMVertInput) throws IllegalArgumentException {
        try {
            idPWMHoriz = Objects.requireNonNull(idPWMHorizInput);
            idPWMVert = Objects.requireNonNull(idPWMVertInput);

        } catch (NullPointerException e) {
            logger.error("Paramètres null...");
            throw new IllegalArgumentException(getClass().getSimpleName() + " : Param(s) null");
        }
        idPWMHoriz = idPWMHorizInput;
        idPWMVert = idPWMVertInput;
        pwmControler = ServoControlDriver.getSingleRef();
        pwmControler.setServoPulseCameraHor(centerAxe0);
        pwmControler.setServoPulseCameraVer(centerAxe1);
        logger.info("Création de la caméra servo branchée sur : " + idPWMHoriz + " & " + idPWMVert);
    }
    */

    // Conversion d'une valeur d'axe (-100 à 100) en impulsion de servo (-50 à 50 ou -300 à 300)
    public int AxisToPulse(int x) {
        // Quantifier x par pas de 20 (résultat entier entre -5 et 5)
        x = (x / 20);
        // Convertir en entier entre -100 et 100 (pas pour un système d'incrémentation)
        int output = x * 20 * 2;
        if (modeDeplacement == 1) {
            // Convertir en entier entre -300 et 300 (pas pour réinitialisation à chaque fois)
            output *= 3;
        }            
        return output;
    }

    // Rotation axe horizontal des servos
    public void rotationAxeHori(Integer val) throws IllegalArgumentException {
        if (val == null) {
            logger.error("Paramètres null...");
            throw new IllegalArgumentException("La valeur ne peut pas être null.");
        }
        switch (modeDeplacement) {
            // Mode incrémentation
            case 0 -> {
                servo0Position += AxisToPulse(-val);
            }
            // Mode absolu
            case 1 -> {
                servo0Position = AxisToPulse(-val);
            }
            default -> {
                logger.error("Mode de déplacement inconnu...");
                throw new IllegalArgumentException("Mode de déplacement inconnu.");
            }
        }
        // Limitation des positions des servos
        if (servo0Position > maxAxe0) servo0Position = maxAxe0;
        if (servo0Position < minAxe0) servo0Position = minAxe0;
        // Appliquer les commandes aux servos
        pwmControler.setServoPulseCameraHor(servo0Position);
        // Logging de l'action
        logger.debug("Rotation axe horizontal de la " + idPWMHoriz + " à la position : " + servo0Position);
    }

    // Rotation axe vertical des servos
    public void rotationAxeVert(Integer val) throws IllegalArgumentException {
        if (val == null) {
            logger.error("Paramètres null...");
            throw new IllegalArgumentException("La valeur ne peut pas être null.");
        }
        switch (modeDeplacement) {
            // Mode incrémentation
            case 0 -> {
                servo1Position += AxisToPulse(-val);
            }
            // Mode absolu
            case 1 -> {
                servo1Position = AxisToPulse(-val);
            }
            default -> {
                logger.error("Mode de déplacement inconnu...");
                throw new IllegalArgumentException("Mode de déplacement inconnu.");
            }
        }
        // Limitation des positions des servos
        if (servo1Position > maxAxe1) servo1Position = maxAxe1;
        if (servo1Position < minAxe1) servo1Position = minAxe1;
        // Appliquer les commandes aux servos
        pwmControler.setServoPulseCameraVer(servo1Position);
        // Logging de l'action
        logger.debug("Rotation axe vertical de la " + idPWMVert + " à la position : " + servo1Position);
    }

    // Réinitialisation des positions des servos
    public void resetPosition() {
        servo0Position = centerAxe0;
        servo1Position = centerAxe1;
        // Appliquer les commandes aux servos
        pwmControler.setServoPulseCameraHor(servo0Position);
        pwmControler.setServoPulseCameraVer(servo1Position);
        // Logging de l'action
        logger.debug("Reset position servos de la caméra branché sur : " + idPWMHoriz + " & " + idPWMVert);
    }

    // Changement de mode de déplacement des servos
    public void switchMode() {
        resetPosition();
        logger.debug("Changement de mode de déplacement des servos de la caméra branché sur : " + idPWMHoriz + " & " + idPWMVert);
        if (modeDeplacement == 0) {
            modeDeplacement = 1;
            logger.debug("Mode de déplacement absolu activé.");
        } else {
            modeDeplacement = 0;
            logger.debug("Mode de déplacement par incrémentation activé.");
        }
    }

    private static Logger logger = LogManager.getLogger(ServoCameraBusiness.class);

}
