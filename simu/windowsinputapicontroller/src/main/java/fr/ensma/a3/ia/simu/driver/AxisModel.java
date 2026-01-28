package fr.ensma.a3.ia.simu.driver;

public final class AxisModel {

    /* ===== XInput / axes normalizados ===== */
    private float lx, ly;
    private float rx, ry;
    private float lt, rt;

    /* ===== DirectInput raw / normalizados no driver ===== */
    private float di_lx, di_ly;
    private float di_lz, di_lrz;
    private float di_s0, di_s1;

    /* ===== Getters XInput ===== */
    public float getLx() {
        return lx;
    }

    public float getLy() {
        return ly;
    }

    public float getRx() {
        return rx;
    }

    public float getRy() {
        return ry;
    }

    public float getLt() {
        return lt;
    }

    public float getRt() {
        return rt;
    }

    /* ===== Getters DirectInput ===== */
    public float getDix() {
        return di_lx;
    }

    public float getDiy() {
        return di_ly;
    }

    public float getDiz() {
        return di_lz;
    }

    public float getDiRz() {
        return di_lrz;
    }

    public float getDiS0() {
        return di_s0;
    }

    public float getDiS1() {
        return di_s1;
    }

    /* ===== Setter global (appelé par le driver) ===== */
    public void set(
            float lx, float ly,
            float rx, float ry,
            float lt, float rt,

            float di_lx, float di_ly,
            float di_lz, float di_lrz,
            float di_s0, float di_s1
    ) {
        this.lx = lx;
        this.ly = ly;
        this.rx = rx;
        this.ry = ry;
        this.lt = lt;
        this.rt = rt;

        this.di_lx = di_lx;
        this.di_ly = di_ly;
        this.di_lz = di_lz;
        this.di_lrz = di_lrz;
        this.di_s0 = di_s0;
        this.di_s1 = di_s1;
    }

    public void clear() {
        set(
                0f, 0f,
                0f, 0f,
                0f, 0f,
                0f, 0f,
                0f, 0f,
                0f, 0f
        );
    }
}
