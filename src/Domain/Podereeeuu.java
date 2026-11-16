package Domain;


public class Podereeeuu extends Poder {
    public Podereeeuu() { super("EEUU", 6); }

    @Override public double getVelocidadTiro() { return 22.0; }

    @Override protected Efecto crearEfecto() { return new Efectoeeuu(); }

}