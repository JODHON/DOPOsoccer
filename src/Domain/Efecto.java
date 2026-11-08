package Domain;

/** Efecto temporal sobre un jugador. Cada poder crea su propio tipo de efecto. */
public abstract class Efecto {
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
}
