package fr.ensma.a3.ia.simu.driver;

public interface IWindowsInputAPIController extends AutoCloseable {

    boolean poll();

    AxisModel axis();

    ButtonsModel buttons();

    void requestRumble(float left01, float right01);

    boolean isConnected();

    @Override
    void close();
}
