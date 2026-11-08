package Domain;

import java.util.ArrayList;
import java.util.List;


public class Balonfalso extends Balon {
    private static final double DURACION = 300;   // frames (5 s a 60 fps)
    private static final double VELOCIDAD = 10.0;
    private double restante = DURACION;

    public Balonfalso(double x, double y, int size, double rebote, double velocidad, double angulo) {
        super(x, y, size, rebote);
        golpear(velocidad, angulo);
    }

    @Override public void actualizar(double dt) {
        super.actualizar(dt);
        restante -= dt;
    }

    public boolean haExpirado() { return restante <= 0; }


    public static List<Balonfalso> abanico(Balon origen, double anguloBase, int n) {
        List<Balonfalso> lista = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double desvio = (i % 2 == 0 ? -1 : 1) * 0.5 * (i / 2 + 1); // -0.5, +0.5, -1.0, +1.0...
            lista.add(new Balonfalso(origen.getX(), origen.getY(), origen.getSize(), 0.75,
                                     VELOCIDAD, anguloBase + desvio));
        }
        return lista;
    }
}