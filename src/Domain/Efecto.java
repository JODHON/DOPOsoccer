package Domain;

import java.io.Serializable;

import java.util.Collections;
import java.util.List;



public abstract class Efecto implements Serializable {
    private final String nombre;
    private double restante; // frames

    protected Efecto(String nombre, double duracion) {
        this.nombre = nombre;
        this.restante = duracion;
    }

    public abstract void aplicar(Jugador j);
    public abstract void quitar(Jugador j);

    public void actualizar(double dt) { restante -= dt; }
    public boolean haExpirado() { return restante <= 0; }
    public String getNombre() { return nombre; }


    public void alImpactar(Balon real) { }

    /** Balones falsos que este efecto mantiene en juego (vacio salvo en Ilusion). */
    public List<Balonfalso> getFalsos() { return Collections.emptyList(); }
}