package Domain;

public class Podercorea extends Poder{
    public Podercorea() {
        super("Corea",5); // 4 x 5 = 20 de dano base
    }

    @Override public double getVelocidadTiro() { return 22.0; }

    @Override protected Efecto crearEfecto() { return new Efectocorea(); }
}
