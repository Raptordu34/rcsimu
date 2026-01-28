package fr.ensma.a3.ia.servocontrolbusiness;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * This class stores all the useful data for the {@link ServoControlBusinessAPI}.
 * Create a new instance in the service layer of this class and set values
 * gotten by the "bigData", then launch your commands with the {@link IServoControlBusinessAPI}
 * @version 1.0
 */
public class DriverData {

    private Integer start;
    private Integer accelerate;
    private Integer reverse;
    private Integer direction;
    private Integer brake;
    private Integer modeup;
    private Integer modedown;
    private Integer accelrevers;
    private EAxisInputType axisInputType;

    private Boolean invertAccel = false;

    public DriverData (final EAxisInputType axisInputType_e){
        start = 0;
        axisInputType = axisInputType_e;
        accelerate = -100;
        reverse = -100;
        accelrevers = 0;
        direction = 0;
        brake = 0;
        modedown = 0;
        modeup = 0;
        logger.info("Driver data initialized : " + toString());
    }

    public DriverData (final DriverData driver) {
        Objects.requireNonNull(driver, "driver cannot be null");
        updateFrom(driver);
        logger.info("Driver data initialized from another driver : " + driver.toString());
    }

    // --- Getters ---
    public Integer getStart() {
        return start;
    }

    public Integer getAccelerate() {
        return accelerate;
    }

    public Integer getReverse() {
        return reverse;
    }

    public Integer getDirection() {
        return direction;
    }

    public Integer getBrake() {
        return brake;
    }

    public Integer getModeup(){
        return modeup;
    }

    public Integer getModedown(){
        return modedown;
    }

    public Integer getAccelrevers(){
        return accelrevers;
    }

    public EAxisInputType getAxisInputType() {
    return axisInputType;
    }

    public Boolean getInvertAccel() {
        return invertAccel;
    }

    // --- Setters ---
    public void setStart(final Integer value) {
        Objects.requireNonNull(value);
        start = value;
        logger.debug("start set to " + start);
    }

    public void setAccelerate(final Integer value) {
        Objects.requireNonNull(value);
        if (!invertAccel) {
            accelerate = value;
        } else {
            reverse = value;
        }
        logger.debug("accelerate set to " + accelerate);
    }

    public void setReverse(final Integer value) {
        Objects.requireNonNull(value);
        if (!invertAccel) {
            reverse = value;
        } else {
            accelerate = value;
        }
        logger.debug("reverse set to " + reverse);
    }

    public void setDirection(final Integer value) {
        Objects.requireNonNull(value);
        direction = value;
        logger.debug("direction set to " + direction);
    }

    public void setBrake(final Integer value) {
        Objects.requireNonNull(value);
        brake = value;
        logger.debug("brake set to " + brake);
    }

    public void setModeup(final Integer value) {
        Objects.requireNonNull(value);
        modeup = value;
        logger.debug("modeup set to " + modeup);
    }

    public void setModedown(final Integer value) {
        Objects.requireNonNull(value);
        modedown = value;
        logger.debug("modedown set to " + modedown);
    }

    public void setAccelrevers(final Integer value) {
        Objects.requireNonNull(value);
        if (!invertAccel) {
            accelrevers = value;
        } else {
            accelrevers = -value;
        }
        logger.debug("accelrevers set to " + accelrevers);
    }

    public void setAxisInputType(EAxisInputType axisType) {
        Objects.requireNonNull(axisType);
        axisInputType = axisType;
        logger.debug("axisInputType set to " + axisInputType);
    }

    public void setInvertAccel(final Boolean bool) {
        Objects.requireNonNull(bool);
        invertAccel = bool;
        logger.debug("invertAccel set to " + bool);
    }
    
     // --- Update values from another DriverData object ---
    public void updateFrom(DriverData other) {
        Objects.requireNonNull(other, "other DriverData cannot be null");
        setStart(other.getStart());
        setAccelerate(other.getAccelerate());
        setReverse(other.getReverse());
        setDirection(other.getDirection());
        setBrake(other.getBrake());
        setModedown(other.getModedown());
        setModeup(other.getModeup());
        setAccelrevers(other.getAccelrevers());
        setAxisInputType(other.getAxisInputType());
        setInvertAccel(other.getInvertAccel());
        logger.debug("values updated from " + other.toString());
    }
    
    
    @Override
    public String toString() {
        return "DriverData [start=" + start + ", accelerate=" + accelerate + ", reverse=" + reverse + ", direction="
                + direction + ", brake=" + brake + ", modeup=" + modeup + ", modedown=" + modedown + ", accelrevers="
                + accelrevers + ", axisInputType=" + axisInputType + "]";
    }


    private static final Logger logger = LogManager.getLogger(DriverData.class);
}
