package Domain;

import java.util.ArrayList;
import java.util.List;


public class Balonfalso extends Balon {

    public Balonfalso(double x, double y, int size, double rebote, double velocidad, double angulo) {
        super(x, y, size, rebote);
        golpear(velocidad, angulo);
    }


    public static List<Balonfalso> abanico(Balon real, int n) {
        double velocidad = Math.hypot(real.getVx(), real.getVy());
        double base = Math.atan2(real.getVy(), real.getVx());
        List<Balonfalso> lista = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double desvio = (i % 2 == 0 ? -1 : 1) * 0.2 * (i / 2 + 1); // -0.2, +0.2, -0.4, +0.4, -0.6, +0.6, -0.8
            lista.add(new Balonfalso(real.getX(), real.getY(), real.getSize(), 0.75, velocidad, base + desvio));
        }
        return lista;
    }
}