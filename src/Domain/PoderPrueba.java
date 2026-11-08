package Domain;

/** TEMPORAL: poder de prueba para probar el aura y el tiro. Borrar cuando existan los poderes reales. */
public class PoderPrueba extends Poder {
    public PoderPrueba() { super("Prueba", 4); } // 4 x 5 = 20 de dano base

    @Override public double getVelocidadTiro() { return 22.0; }

    /** Sin efecto: solo hace dano (aplicarEfecto acepta null). */
    @Override protected Efecto crearEfecto() { return null; }
}
