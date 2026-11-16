package Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Ilusion (EEUU): el balon se divide en 8 identicos (1 real + 7 falsos) durante 2.5 s.
 * Los falsos desaparecen cuando termina el efecto.
 */
public class Efectoeeuu extends Efecto {
    private static final int CANTIDAD = 7;

    private final List<Balonfalso> falsos = new ArrayList<>();
    private Balon real;
    private boolean creados = false;

    public Efectoeeuu() { super("Ilusión", 150); } // 2.5 s a 60 fps

    @Override public void alImpactar(Balon real) { this.real = real; }
    @Override public void aplicar(Jugador j) { }
    @Override public void quitar(Jugador j)  { falsos.clear(); }

    @Override public void actualizar(double dt) {
        super.actualizar(dt);
        // Los falsos se crean en el frame siguiente al impacto, cuando el real ya salio rebotado:
        // asi copian su velocidad y se ven iguales.
        if (!creados && real != null) {
            creados = true;
            if (real.getVx() != 0 || real.getVy() != 0) {
                falsos.addAll(Balonfalso.abanico(real, CANTIDAD));
            }
        }
        for (Balonfalso f : falsos) f.actualizar(dt);
    }

    @Override public List<Balonfalso> getFalsos() { return falsos; }
}