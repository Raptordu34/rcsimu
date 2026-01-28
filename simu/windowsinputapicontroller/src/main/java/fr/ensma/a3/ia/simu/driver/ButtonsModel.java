package fr.ensma.a3.ia.simu.driver;

public final class ButtonsModel {

    private int mask;

    public int getMask() { return mask; }

    public void setMask(int mask) { this.mask = mask; }

    public boolean isPressed(int bitIndex) {
        if (bitIndex < 0 || bitIndex > 31) return false;
        return (mask & (1 << bitIndex)) != 0;
    }

    public void clear() {
        mask = 0;
    }
}
