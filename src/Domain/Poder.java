package Domain;

import java.io.Serializable;


public abstract class Poder implements Serializable {

    private static final double FACTOR_ESCALA_DAÑO=5;

    private final String nombre;
    private final double daño;


    protected Poder(String nombre, double dañoTiro) {
        this.nombre = nombre;
        this.daño = dañoTiro * FACTOR_ESCALA_DAÑO;
    }

    // Lo que cambia por pais
    public abstract double getVelocidadTiro();
    protected abstract Efecto crearEfecto();


    public double getAnguloTiro(boolean haciaDerecha) {
        return haciaDerecha ? 0 : Math.PI;
    }


    public void lanzar(Balon balon, Jugador lanzador, boolean haciaDerecha) {
        balon.golpear(getVelocidadTiro(), getAnguloTiro(haciaDerecha));
        balon.activarPoder(this, lanzador);
    }


    public final void aplicarImpacto(Jugador rival, Jugador lanzador, Balon balon) {
        rival.recibirDaño(daño * lanzador.getStats().getMultiplicadorKick());
        Efecto efecto = crearEfecto();
        if (efecto != null) efecto.alImpactar(balon);
        rival.aplicarEfecto(efecto);
    }
    


    public String getNombre() { return nombre; }
    public double getdaño()  { return daño; }
}