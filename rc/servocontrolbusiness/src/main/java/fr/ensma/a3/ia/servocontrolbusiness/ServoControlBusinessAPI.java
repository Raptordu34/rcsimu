package fr.ensma.a3.ia.servocontrolbusiness;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.ensma.a3.ia.servohatdriver.ServoControlDriver;
import fr.ensma.a3.ia.servohatdriver.IServoControlDriver;

/**
 * This class contains all the logic and send the data received to the API layer-below
 * 
 * @version 1.0
 */
public class ServoControlBusinessAPI implements IServoControlBusinessAPI {

    private IServoControlDriver panama;

    private Integer motorDutyPercent;
    private Integer directionDutyPercent;
    private Integer mode;

    private Integer accelerationValue;
    private Integer reverseValue;
    private Boolean brakeValue;
    private Boolean startValue;

    private Integer modePercentCap = 100;

    private DriverData previousValues;

    /* ========= ÉTAT MACHINE FREINAGE/REVERSE ========= */
    // true = on a freiné et relâché depuis la dernière avance, prêt pour reverse
    private boolean readyForReverse = false;
    // Seuil de vitesse considéré comme "arrêté" pour autoriser la marche arrière
    private static final int STOPPED_THRESHOLD = 5;

    /**
     * Initialize the API
     */
    public ServoControlBusinessAPI () {
        startValue = false;
        panama = ServoControlDriver.getSingleRef();
        if (panama.initDriver() < 0) {
            logger.error("Failed to initialize driver");
        }
        setMode(3);
        /* 
        if (panama.initGpioReading() < 0) {
            logger.error("Failed to initialize GPIO reading");
        }
            */
        logger.info("Driver API initialized");
    }

    private Integer linearInt(Integer x, Integer x0, Integer x1, Integer y0, Integer y1) {
        Objects.requireNonNull(x);
        Objects.requireNonNull(x0);
        Objects.requireNonNull(x1);
        Objects.requireNonNull(y0);
        Objects.requireNonNull(y1);
        if (x0 == x1) {
            return y0;
        } else {
            return y0 + ((x - x0) * (y1 - y0)) / (x1 - x0);
        }
    }
    
    //accelerate : axis value, so -100 -> 0, 100 -> 100
    private void accelerateConvert(final Integer value) {
        Objects.requireNonNull(value);
        accelerationValue = linearInt(value, -100, 100, 0, 100);
        logger.debug("axis conversion; val : " + value + "accel : " + accelerationValue);
    }

    //reverse : axis value, so -100 -> 0, 100 -> 100
    private void reverseConvert(final Integer value) {
        Objects.requireNonNull(value);
        reverseValue = linearInt(value, -100, 100, 0, 100);
        logger.debug("axis conversion; val : " + value + "reverse : " + reverseValue);
    }

    private void reverseAccelConvert(final Integer value) {
        Objects.requireNonNull(value);
        if (value > 0) {
            accelerationValue = value;
            reverseValue = 0;
        } else {
            accelerationValue = 0;
            reverseValue = -value;
        }
        logger.debug("axis conversion; val : " + value + "accel : " + accelerationValue + "reverse : " + reverseValue);
    }

    private void reverse(final Integer percent) {
        if (motorDutyPercent != null && motorDutyPercent == -percent) {
            return;
        }

        Integer capped = Math.min(Math.max(percent, 0), modePercentCap);
        if (panama.setMotorDutyEscLrp(-capped) != 0) {
                logger.error("Motor duty ESC failed");
                return;
        }
        motorDutyPercent = -capped;

    }

    private void accelerate(final Integer percent) {
        if (motorDutyPercent != null && motorDutyPercent == percent) {
            return;
        }

        Integer capped = Math.min(Math.max(percent, 0), modePercentCap);
        if (panama.setMotorDutyEscLrp(capped) != 0) {
                logger.error("Motor duty ESC failed");
                return;
        }
        motorDutyPercent = capped;

    }

    /**
     * Freinage dynamique proportionnel - RÉACTIF, sans délai bloquant.
     * Envoie un signal négatif proportionnel à la force de freinage demandée.
     * L'ESC interprète ce signal comme un frein moteur (court-circuit).
     *
     * @param brakePercent intensité du freinage (0-100)
     */
    private void dynamicBrake(final Integer brakePercent) {
        if (motorDutyPercent == null) {
            return;
        }

        // Freinage proportionnel : plus on appuie fort, plus le frein est puissant
        Integer brakeForce = Math.min(Math.max(brakePercent, 0), 100);

        // Envoyer signal négatif pour activer le frein dynamique de l'ESC
        // -100 = frein maximum, 0 = pas de frein
        int brakeDuty = -brakeForce;

        if (panama.setMotorDutyEscLrp(brakeDuty) != 0) {
            logger.error("Motor duty ESC failed during dynamic brake");
            return;
        }
        motorDutyPercent = brakeDuty;
        logger.debug("dynamic brake applied: " + brakeDuty + "%");
    }

    /**
     * Ancien freinage avec délai - conservé pour compatibilité (ex: closeDriver)
     */
    private void brake() {
        if (motorDutyPercent == null) {
            return;
        }

        if (motorDutyPercent > 5) {
            if (panama.setMotorDutyEscLrp(-80) != 0) {
                logger.error("Motor duty ESC failed");
                return;
            }
            motorDutyPercent = -80;
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
                logger.error("Brake process interrupted");
            }
        } else if (motorDutyPercent < -5) {
            if (panama.setMotorDutyEscLrp(80) != 0) {
                logger.error("Motor duty ESC failed");
                return;
            }
            motorDutyPercent = 80;
            try {
                Thread.sleep(175);
            } catch (InterruptedException ignored) {
                logger.error("Brake process interrupted");
            }
        }
        neutral();
    }

    private void direction(Integer percent) {
        if (directionDutyPercent != null && directionDutyPercent == percent) {
            return;
        }

        Integer capped = Math.min(Math.max(percent, -100), 100);
        if (panama.setServoDutyDirection(capped) != 0) {
                logger.error("Direction duty servo failed");
                return;
        }
        directionDutyPercent = capped;

    }

    private void neutral() {
        if (motorDutyPercent != null && motorDutyPercent == 0 ) {
            return;
        }
        if (panama.setMotorDutyEscLrp(0) != 0) {
            logger.error("Motor duty ESC failed");
            return;
        }
        motorDutyPercent = 0;
    }

    private void setMode(final Integer val) {
        mode = val;

        switch (mode) {
            case 0:
                modePercentCap = 15;
                break;

            case 1:
                modePercentCap = 30;
                break;

            case 2:
                modePercentCap = 50;
                break;

            case 3:
                modePercentCap = 100;
                break;
        
            default:
                break;
        }
        logger.debug("mode switched to : " + mode + ", cap : " + modePercentCap);
    }

    @Override
    public void launchCommands(final DriverData driver) {

        Objects.requireNonNull(driver);
        logger.debug("Start launching commands with driver");

        if ((driver.getStart() > 0) && !startValue) {
            startValue = true;
            previousValues = new DriverData(driver);
            logger.debug("car started");
            return;
        } 
        
        //if possible separate each function and use it in different thread
        //for now just a function that does everything
        if (startValue) {

            //thread for modes
            if ((driver.getModedown() > 0) && 
                (mode > 0) &&
                (previousValues.getModedown() != driver.getModedown())) {
                logger.debug("new mode sent : " + Integer.toString(mode - 1));
                setMode(mode - 1);
            }

            if ((driver.getModeup() > 0) && 
                (mode < 3) &&
                (previousValues.getModeup() != driver.getModeup())) {
                logger.debug("new mode sent : " + mode + 1);
                setMode(mode + 1);
            }

            if (driver.getAxisInputType() == EAxisInputType.DUAL_AXIS) {
                accelerateConvert(driver.getAccelerate());
                reverseConvert(driver.getReverse());
            } else {
                reverseAccelConvert(driver.getAccelrevers());
            }

            //thread for directions
            direction(driver.getDirection());
            logger.debug("direction sent : " + driver.getDirection());
            brakeValue = driver.getBrake() > 0;

            //another thread for dc motor
            // Machine à états pour freinage/reverse :
            // 1. Accélération → reset état reverse
            // 2. Frein pendant avance → freinage dynamique
            // 3. Relâcher frein après freinage → prêt pour reverse
            // 4. Frein quand prêt → marche arrière

            if (accelerationValue > STOPPED_THRESHOLD) {
                // On accélère : reset l'état reverse, on n'est plus prêt pour reculer
                readyForReverse = false;
                logger.debug("accelerate sent : " + accelerationValue);
                accelerate(accelerationValue);

            } else if (reverseValue > STOPPED_THRESHOLD) {
                // Le bouton frein/reverse est appuyé

                if (motorDutyPercent != null && motorDutyPercent > STOPPED_THRESHOLD) {
                    // CAS 1: On avançait → FREINAGE DYNAMIQUE (réactif, immédiat)
                    logger.debug("dynamic brake sent (was moving forward): " + reverseValue);
                    dynamicBrake(reverseValue);
                    readyForReverse = false;  // Pas encore prêt, on freine

                } else if (readyForReverse) {
                    // CAS 2: On était arrêté ET on a déjà freiné+relâché → MARCHE ARRIÈRE
                    logger.debug("reverse sent (ready for reverse): " + reverseValue);
                    reverse(reverseValue);

                } else {
                    // CAS 3: On est lent/arrêté mais pas encore passé par le cycle frein+relâche
                    // → continuer à freiner pour s'assurer de l'arrêt complet
                    logger.debug("braking to stop: " + reverseValue);
                    dynamicBrake(reverseValue);
                }

            } else {
                // Ni accélération ni frein : neutre
                // Si on était en train de freiner, on devient prêt pour reverse
                if (motorDutyPercent != null && motorDutyPercent < -STOPPED_THRESHOLD) {
                    // On était en freinage, maintenant on relâche → prêt pour reverse
                    readyForReverse = true;
                    logger.debug("brake released, ready for reverse");
                }
                logger.debug("neutral sent");
                neutral();
            }
            previousValues.updateFrom(driver);
        } else {
            logger.debug("please trigger start button");
        }
    }

    @Override
    public void closeDriver() {
        brake();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            // TODO: handle exception
            logger.error("error during sleep close driver");
        }
        panama.closeDriver();
        panama.closeGpioReading();
        logger.info("driver closed");
    }

    @Override
    public void sendManualMotorDuty(final Integer duty) {
        Objects.requireNonNull(duty);
        panama.setMotorDutyEscLrpManual(duty);
    }

    @Override
    public void sendManualServoDuty(final Integer duty) {
        Objects.requireNonNull(duty);
        panama.setServoDutyDirectionManual(duty);
    }

    @Override
    public void updateDriverByRc(DriverData driver) {

        Integer currentPercentMotor = panama.readPwmPercentGpioMotor();
        logger.debug("motor value red :" + currentPercentMotor);

        if (currentPercentMotor >= -100) {
            driver.setAxisInputType(EAxisInputType.SINGLE_AXIS);
            driver.setAccelrevers(currentPercentMotor);
        } else {
            logger.warn("rc motor not connected or error");
        }

        Integer currentPercentServo = panama.readPwmPercentGpioServo();
        logger.debug("servo value red :" + currentPercentServo);

        if (currentPercentServo >= -100) {
            driver.setDirection(currentPercentServo);
        } else {
            logger.warn("rc direction not connected or error");
        }
        
        
    }

    @Override
    public Integer getMotorPercent() {
        return motorDutyPercent;
    }

    @Override
    public Integer getDirectionPercent() {
        return directionDutyPercent;
    }

    @Override
    public Integer getCurrentMode() {
        return mode;
    }

    @Override
    public String toString() {
        return 
            "======== Values from driver ===========" +
            "\nmotor value : " + motorDutyPercent +
            "\ndirection value : " + directionDutyPercent +
            "\nmode value : " + mode +
            "\n==========Values from api===============" +
            "\nacceleration api : " + accelerationValue +
            "\nreverse api : " + reverseValue +
            "\nstart api : " + startValue +
            "\nmode percent cap value : " + modePercentCap +
            "\nbrake api : " + brakeValue;
    }

    private static final Logger logger = LogManager.getLogger(ServoControlBusinessAPI.class);
}
