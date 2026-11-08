package Domain;

/**
 * Guarda cuantos puntos de mejora tiene cada stat y calcula los valores derivados.
 * Cada punto de mejora suma 10 % del valor BASE. El precio se duplica con cada mejora (500, 1000, 2000...).
 */
public class Estadisticas {
    public enum Stat { SPEED, KICK, JUMP, SURVIVAL }

    public static final int NIVEL_MAX = 10;
    public static final int MAX_MEJORAS = NIVEL_MAX - 1;
    private static final double DAÑO_BASE_PATADA = 10;
    public static final int PRECIO_INICIAL = 500;
    private static final double BONO_POR_MEJORA = 0.10;

    private static final double BASE_SPEED = 5.0;
    private static final double BASE_JUMP = 12.0;  // magnitud (positiva)
    private static final double BASE_VIDA = 100.0;

    private final int[] mejoras = new int[Stat.values().length];

    private double escalar(double base, Stat s) {
        return base * (1 + BONO_POR_MEJORA * mejoras[s.ordinal()]);
    }

    public double getVelocidad()    { return escalar(BASE_SPEED, Stat.SPEED); }
    /** Tabla 3: 1.0 (nivel 1) ... 1.9 (nivel 10). Escala patada y tiros de poder. */
    public double getMultiplicadorKick() { return 1 + BONO_POR_MEJORA * mejoras[Stat.KICK.ordinal()]; }
    /** Daño de patada normal: 10 x multiplicadorKick (10 ... 19). */
    public double getdañoPatada()  { return DAÑO_BASE_PATADA * getMultiplicadorKick(); }
    public double getFuerzaSalto()  { return escalar(BASE_JUMP, Stat.JUMP); }
    public double getVidaMaxima()   { return escalar(BASE_VIDA, Stat.SURVIVAL); }

    public int getMejoras(Stat s)   { return mejoras[s.ordinal()]; }
    public int getNivel(Stat s)     { return mejoras[s.ordinal()] + 1; }
    public boolean estaAlMaximo(Stat s) { return mejoras[s.ordinal()] >= MAX_MEJORAS; }

    /** Precio de la SIGUIENTE mejora de esa stat: 500, 1000, 2000... */
    public int getCostoSiguiente(Stat s) {
        return PRECIO_INICIAL * (1 << mejoras[s.ordinal()]);
    }

    /** Sube un punto de mejora (no cobra: el cobro lo hace Jugador.comprarMejora). */
    public boolean subir(Stat s) {
        if (estaAlMaximo(s)) return false;
        mejoras[s.ordinal()]++;
        return true;
    }
}