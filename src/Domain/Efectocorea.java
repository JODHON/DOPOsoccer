package Domain;

public class Efectocorea extends Efecto{

    private static final double VELOCIDAD_EMPUJE = 6.0; // px por frame
    private Jugador jugador;

    public Efectocorea() { super("Empujado", 120); }

    @Override public void aplicar(Jugador j) { jugador = j; }
    @Override public void quitar(Jugador j)  { }

    @Override public void actualizar(double dt) {
        super.actualizar(dt); // mantiene el conteo de duración
        int dir = (jugador.getLado() == 1) ? -1 : 1; // lado 1: arco izquierdo, lado 2: arco derecho
        jugador.moverJugador(dir * VELOCIDAD_EMPUJE * dt);
    }
}
