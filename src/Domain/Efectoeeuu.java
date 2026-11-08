package Domain;

import java.util.List;

/** Ilusion: al impactar, aparecen 7 balones falsos alrededor del rival. */
public class Efectoeeuu extends Efecto {
    private static final int CANTIDAD = 7;
    private static final double VELOCIDAD = 10.0;

    private Jugador jugador;
    private List<Balonfalso> falsos;
    private boolean lanzados = false;

    public Efectoeeuu() { super("Ilusión", 120); }

    @Override public void aplicar(Jugador j) { jugador = j; }
    @Override public void quitar(Jugador j)  { }

   // @Override public void conectarFalsos(List<Balonfalso> falsos) { this.falsos = falsos; }

    @Override public void actualizar(double dt) {
        super.actualizar(dt); // mantiene el conteo de duracion (sin esto el efecto nunca expira)
        if (lanzados || falsos == null) return;
        lanzados = true; // una sola vez, no 7 por frame

        double cx = jugador.getX() + jugador.getAncho() / 2.0 - 40; // 40 = mitad del balon (80)
        double cy = jugador.getY();
        for (int i = 0; i < CANTIDAD; i++) {
            // abanico hacia arriba: de -170 a -10 grados
            double angulo = -Math.PI + 0.3 + i * (Math.PI - 0.6) / (CANTIDAD - 1);
            falsos.add(new Balonfalso(cx, cy, 80, 0.75, VELOCIDAD, angulo));
        }
    }
}