package Domain;

/** Tiro de poder. Cada pais hereda y define velocidad, trayectoria (opcional) y efecto. */
public abstract class Poder {
    /** Los danos base de la tabla de poderes se multiplican por 5 para ajustarse a la escala de vida. */
    private static final double FACTOR_ESCALA_DAÑO=5;

    private final String nombre;
    private final double daño;

    /** @param dañoTiro DanoTiro tal como aparece en la tabla de Personajes y Poderes (sin escalar). */
    protected Poder(String nombre, double dañoTiro) {
        this.nombre = nombre;
        this.daño = dañoTiro * FACTOR_ESCALA_DAÑO;
    }

    // Lo que cambia por pais
    public abstract double getVelocidadTiro();
    protected abstract Efecto crearEfecto();

    /** Trayectoria por defecto: recta. Se puede sobrescribir. */
    public double getAnguloTiro(boolean haciaDerecha) {
        return haciaDerecha ? 0 : Math.PI;
    }

    /** Ejecuta el tiro: lanza el balon y lo marca con el poder y su lanzador. */
    public void lanzar(Balon balon, Jugador lanzador, boolean haciaDerecha) {
        balon.golpear(getVelocidadTiro(), getAnguloTiro(haciaDerecha));
        balon.activarPoder(this, lanzador);
    }

    /** Comun a todos: dano (DanoTiro x multiplicadorKick del lanzador) + efecto (el efecto nuevo reemplaza al anterior). */
    public final void aplicarImpacto(Jugador rival, Jugador lanzador) {
        rival.recibirDaño(daño * lanzador.getStats().getMultiplicadorKick());
        rival.aplicarEfecto(crearEfecto());
    }
    


    public String getNombre() { return nombre; }
    public double getdaño()  { return daño; }
}