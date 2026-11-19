package Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene las reglas y el estado de un partido.
 * No recibe teclas ni coordina la actualización de los objetos: eso lo hace DOPOsoccer.
 */
public class Partido {

    public enum Estado { JUGANDO, MUERTE_SUBITA, TERMINADO }
    public enum Modo { TIEMPO, GOLES }

    public static final int DURACION_DEFECTO_S = 60;
    public static final int GOLES_DEFECTO = 5;

    private static final int PUNTOS_GANADOR = 100;
    private static final int PUNTOS_PERDEDOR = 25;

    private final Jugador jugador;
    private final Jugador jugador2;
    private final Balon balon;
    private final Cancha canchaizq;
    private final Cancha canchader;

    private final Modo modo;
    private final long duracionNs;
    private final int golesObjetivo;

    private Estado estado = Estado.JUGANDO;
    private Jugador ganador;
    private final long inicioNs;

    private boolean pausado;
    private long inicioPausaNs;
    private long pausaAcumuladaNs;

    public Partido(String pais1, String pais2) throws DopoSoccerException {
        this(pais1, pais2, Modo.TIEMPO, DURACION_DEFECTO_S);
    }

    public Partido(String pais1, String pais2, Modo modo) throws DopoSoccerException {
        this(pais1, pais2, modo,
                modo == Modo.TIEMPO ? DURACION_DEFECTO_S : GOLES_DEFECTO);
    }

    /** limite son segundos en modo TIEMPO o goles en modo GOLES. */
    public Partido(String pais1, String pais2, Modo modo, int limite) throws DopoSoccerException {
        if (modo == null) throw new DopoSoccerException(DopoSoccerException.MODO_NULO);

        if (limite<=0) throw new DopoSoccerException(DopoSoccerException.LIMITE_INVALIDO);
        this.modo = modo;
        this.duracionNs = (modo == Modo.TIEMPO
                ? Math.max(1, limite) : DURACION_DEFECTO_S) * 1_000_000_000L;

        this.golesObjetivo = modo == Modo.GOLES
                ? Math.max(1, limite) : GOLES_DEFECTO;

        jugador = new Jugador(200, 300, 1, pais1);
        jugador2 = new Jugador(880, 300, 2, pais2);
        balon = new Balon(600, 150, 80, 0.75);
        canchaizq = new Cancha(200, 400, 0, 315);
        canchader = new Cancha(200, 400, 1080, 315);
        inicioNs = System.nanoTime();
    }

    // ---------------- PAUSA Y RELOJ ----------------

    public void pausar() {
        if (pausado || estado == Estado.TERMINADO) return;
        pausado = true;
        inicioPausaNs = System.nanoTime();
    }

    public void reanudar() {
        if (!pausado) return;
        pausaAcumuladaNs += System.nanoTime() - inicioPausaNs;
        pausado = false;
    }

    public boolean isPausado() { return pausado; }

    public double getTiempoRestante() {
        if (modo != Modo.TIEMPO || estado != Estado.JUGANDO) return 0;
        long ahora = pausado ? inicioPausaNs : System.nanoTime();
        long transcurrido = ahora - inicioNs - pausaAcumuladaNs;
        return Math.max(0, (duracionNs - transcurrido) / 1e9);
    }

    /** Lo invoca DOPOsoccer una vez por ciclo de actualización. */
    void comprobarFinPorTiempo() {
        if (modo == Modo.TIEMPO && estado == Estado.JUGANDO
                && getTiempoRestante() <= 0) {
            terminarTiempo();
        }
    }

    private void terminarTiempo() {
        if (jugador.getGoles() > jugador2.getGoles()) {
            terminar(jugador);
        } else if (jugador2.getGoles() > jugador.getGoles()) {
            terminar(jugador2);
        } else {
            estado = Estado.MUERTE_SUBITA;
            balon.reiniciar();
        }
    }

    private void terminar(Jugador ganadorPartido) {
        Jugador perdedor = ganadorPartido == jugador ? jugador2 : jugador;
        ganadorPartido.sumarPuntos(PUNTOS_GANADOR);
        perdedor.sumarPuntos(PUNTOS_PERDEDOR);
        ganador = ganadorPartido;
        estado = Estado.TERMINADO;
    }

    // ---------------- GOLES ----------------

    public void registrarGol(Jugador anotador, Jugador receptor) {
        if (estado == Estado.TERMINADO) return;
        anotador.sumarGol();
        receptor.recibirGol();
        balon.reiniciar();
        limpiarFalsos();

        if (estado == Estado.MUERTE_SUBITA) {
            terminar(anotador);
        } else if (modo == Modo.GOLES && anotador.getGoles() >= golesObjetivo) {
            terminar(anotador);
        }
    }

    public List<Balonfalso> getBalonesFalsos() {
        List<Balonfalso> todos = new ArrayList<>();
        for (Jugador j : new Jugador[]{jugador, jugador2}) {
            if (j.getEfectoActivo() != null) {
                todos.addAll(j.getEfectoActivo().getFalsos());
            }
        }
        return todos;
    }

    private void limpiarFalsos() {
        for (Jugador j : new Jugador[]{jugador, jugador2}) {
            if (j.getEfectoActivo() != null) {
                j.getEfectoActivo().getFalsos().clear();
            }
        }
    }

    // ---------------- ACCESO A LOS ELEMENTOS ----------------

    public Jugador getJugador() { return jugador; }
    public Jugador getJugador2() { return jugador2; }
    public Balon getBalon() { return balon; }
    public Cancha getCanchaizq() { return canchaizq; }
    public Cancha getCanchader() { return canchader; }
    public int[] getGoles() { return new int[]{jugador.getGoles(), jugador2.getGoles()}; }
    public Estado getEstado() { return estado; }
    public Modo getModo() { return modo; }
    public int getGolesObjetivo() { return golesObjetivo; }
    public Jugador getGanador() { return ganador; }
}
