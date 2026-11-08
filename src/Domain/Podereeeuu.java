package Domain;


public class Podereeeuu extends Poder {
    public Podereeeuu() { super("EEUU", 4); }

    @Override public double getVelocidadTiro() { return 22.0; }

    @Override protected Efecto crearEfecto() { return null; } // sin efecto

}