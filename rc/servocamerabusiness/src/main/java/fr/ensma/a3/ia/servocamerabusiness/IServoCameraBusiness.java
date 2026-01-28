package fr.ensma.a3.ia.servocamerabusiness;

public interface IServoCameraBusiness {

    public void rotationAxeHori(Integer val) throws IllegalArgumentException;
    public void rotationAxeVert(Integer val) throws IllegalArgumentException;
    public void resetPosition();
    public void switchMode();

}
