package Domain;

public class Podercorea extends Poder{
    public Podercorea() {
        super("Corea", 8); // 8 x 5 = 40 de dano real (Anexo 1)
    }

    @Override public double getVelocidadTiro() { return 22.0; }

    @Override protected Efecto crearEfecto() { return new Efectocorea(); }
}