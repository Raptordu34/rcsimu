package fr.ensma.a3.ia.servocontrolbusiness;

/**
 * Enum to define which type of axis is using the user.
 * @version 1.0
 */
public enum EAxisInputType {
    SINGLE_AXIS,  // Accelerate and reverse use the same axis
    DUAL_AXIS,   // Accelerate and reverse use separate axes
}
