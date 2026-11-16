package Domain;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Partido {

    public enum Estado { JUGANDO, MUERTE_SUBITA, TERMINADO }
    public enum Modo { TIEMPO, GOLES }

    public static final int DURACION_DEFECTO_S = 60;
    public static final int GOLES_DEFECTO = 5;

    private static final int PUNTOS_GANADOR = 100;
    private static final int PUNTOS_PERDEDOR = 25;

    private Jugador jugador;   // Jugador 1: A, D, W | F patear, G slide, H poder
    private Jugador jugador2;  // Jugador 2: flechas | , patear, . slide, / poder
    private Balon balon;
    private Cancha canchaizq;
    private Cancha canchader;

    private final Modo modo;
    private final long duracionNs;
    private final int golesObjetivo;

    private Estado estado = Estado.JUGANDO;
    private Jugador ganador;
    private final long inicioNs;

    // Pausa: el reloj se mide con nanoTime, asi que se descuenta el tiempo pausado
    private boolean pausado;
    private long inicioPausaNs;
    private long pausaAcumuladaNs;

    private boolean izq1, der1, izq2, der2;

    public Partido(String pais1, String pais2) {
        this(pais1, pais2, Modo.TIEMPO, DURACION_DEFECTO_S);
    }

    public Partido(String pais1, String pais2, Modo modo) {
        this(pais1, pais2, modo, modo == Modo.TIEMPO ? DURACION_DEFECTO_S : GOLES_DEFECTO);
    }

    /**
     * @param limite segundos de juego si el modo es TIEMPO, o goles para ganar si el modo es GOLES
     */
    public Partido(String pais1, String pais2, Modo modo, int limite) {
        this.modo = modo;
        this.duracionNs = (modo == Modo.TIEMPO ? Math.max(1, limite) : DURACION_DEFECTO_S) * 1_000_000_000L;
        this.golesObjetivo = (modo == Modo.GOLES) ? Math.max(1, limite) : GOLES_DEFECTO;

        this.jugador = new Jugador(200, 300, 1, pais1);
        this.jugador2 = new Jugador(880, 300, 2, pais2);
        this.balon = new Balon(600, 150, 80, 0.75);
        this.canchaizq = new Cancha(200, 400, 0, 315);
        this.canchader = new Cancha(200, 400, 1080, 315);
        this.inicioNs = System.nanoTime();
    }

    public void actualizarLogica() {
        if (estado == Estado.TERMINADO || pausado) return;

        jugador.actualizar(1.0);
        jugador2.actualizar(1.0);
        balon.actualizar(1.0);
        verificarColisiones();

        // En modo GOLES no hay reloj: solo termina al llegar al objetivo
        if (modo == Modo.TIEMPO && estado == Estado.JUGANDO && getTiempoRestante() <= 0) {
            terminarTiempo();
        }
    }

    // ---------- Pausa ----------

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

    // ---------- Tiempo y fin de partido ----------

    /** Segundos restantes (0 en muerte subita, al terminar o en modo GOLES). El tiempo en pausa no cuenta. */
    public double getTiempoRestante() {
        if (modo != Modo.TIEMPO || estado != Estado.JUGANDO) return 0;
        long ahora = pausado ? inicioPausaNs : System.nanoTime();
        long transcurrido = ahora - inicioNs - pausaAcumuladaNs;
        return Math.max(0, (duracionNs - transcurrido) / 1e9);
    }

    private void terminarTiempo() {
        if (jugador.getGoles() > jugador2.getGoles()) {
            terminar(jugador);
        } else if (jugador2.getGoles() > jugador.getGoles()) {
            terminar(jugador2);
        } else {
            estado = Estado.MUERTE_SUBITA; // el primero en marcar gana
            balon.reiniciar();
        }
    }

    private void terminar(Jugador g) {
        Jugador perdedor = (g == jugador) ? jugador2 : jugador;
        g.sumarPuntos(PUNTOS_GANADOR);
        perdedor.sumarPuntos(PUNTOS_PERDEDOR);
        ganador = g;
        estado = Estado.TERMINADO;
    }

    // ---------- Teclas ----------

    public void teclaPresionada(int codigo) {
        // Movimiento: se registra siempre, para no perder teclas sostenidas al pausar
        switch (codigo) {
            case KeyEvent.VK_A: izq1 = true; break;
            case KeyEvent.VK_D: der1 = true; break;
            case KeyEvent.VK_LEFT: izq2 = true; break;
            case KeyEvent.VK_RIGHT: der2 = true; break;
        }

        // Acciones: solo si el juego no esta en pausa
        if (!pausado) {
            switch (codigo) {
                case KeyEvent.VK_W: jugador.saltar(); break;
                case KeyEvent.VK_F: jugador.patear(balon, jugador2); break;
                case KeyEvent.VK_G: jugador.dash(); break;
                case KeyEvent.VK_H: jugador.usarPoder(); break;

                case KeyEvent.VK_UP: jugador2.saltar(); break;
                case KeyEvent.VK_COMMA: jugador2.patear(balon, jugador); break;
                case KeyEvent.VK_PERIOD: jugador2.dash(); break;
                case KeyEvent.VK_SLASH: jugador2.usarPoder(); break;
            }
        }
        actualizarMovimiento();
    }

    public void teclaLiberada(int codigo) {
        switch (codigo) {
            case KeyEvent.VK_A: izq1 = false; break;
            case KeyEvent.VK_D: der1 = false; break;
            case KeyEvent.VK_LEFT: izq2 = false; break;
            case KeyEvent.VK_RIGHT: der2 = false; break;
        }
        actualizarMovimiento();
    }

    private void actualizarMovimiento() {
        jugador.mover((der1 ? 1 : 0) - (izq1 ? 1 : 0));
        jugador2.mover((der2 ? 1 : 0) - (izq2 ? 1 : 0));
    }

    // ---------- Colisiones ----------

    private void verificarColisiones() {
        // 1. Un tiro de poder que toca al rival: dano + efecto
        impactoPoder(jugador);
        impactoPoder(jugador2);

        // 2. Contacto jugador-balon (activa tiro de poder si hay aura)
        colisionJugador(jugador);
        colisionJugador(jugador2);

        // 3. Entre jugadores
        if (jugador.getHitbox().intersects(jugador2.getHitbox())) {
            jugador.moverJugador(-1.0);
            jugador2.moverJugador(1.0);
        }

        // 4. Techos: rebota y ese frame no cuenta gol (ya no corta el resto de colisiones)
        boolean tocaTecho = colisionTecho(canchaizq) | colisionTecho(canchader);

        // 5. Goles
        if (!tocaTecho) {
            if (balon.getHitbox().intersects(canchaizq.getRed())) {
                registrarGol(jugador2, jugador);
            } else if (balon.getHitbox().intersects(canchader.getRed())) {
                registrarGol(jugador, jugador2);
            }
        }
    }

    /** Si el balon lleva poder y toca a 'rival' (no al lanzador), se aplica dano y efecto una sola vez. */
    private void impactoPoder(Jugador rival) {
        if (!balon.tienePoderActivo() || balon.getLanzador() == rival) return;
        if (!balon.getHitbox().intersects(rival.getHitbox())) return;

        balon.getPoderActivo().aplicarImpacto(rival, balon.getLanzador(), balon);
        balon.limpiarPoder();
    }

    private void colisionJugador(Jugador j) {
        if (!j.getHitbox().intersects(balon.getHitbox())) return;
        // El balon recien lanzado sigue dentro del hitbox del lanzador: no lo frenamos
        if (balon.tienePoderActivo() && balon.getLanzador() == j) return;

        boolean haciaDerecha = balon.getHitbox().getCenterX() >= j.getHitbox().getCenterX();

        if (j.tienePoder()) {
            j.consumirPoder(); // el aura desaparece
            j.getPoder().lanzar(balon, j, haciaDerecha);
        } else {
            double angulo = haciaDerecha ? -Math.PI / 4 : -3 * Math.PI / 4;
            balon.golpear(Math.hypot(6, 6), angulo);
        }
    }

    private boolean colisionTecho(Cancha c) {
        Rectangle techo = c.getTecho();
        if (!balon.getHitbox().intersects(techo)) return false;

        double nuevaY = (balon.getHitbox().getCenterY() < techo.getCenterY())
                ? techo.y - balon.getSize()
                : techo.y + techo.height;
        balon.rebotarVertical(nuevaY);
        return true;
    }

    /** El gol cuenta siempre; el dano/efecto solo se aplican en impactoPoder (si no toco al rival, no hay). */
    private void registrarGol(Jugador anotador, Jugador receptor) {
        anotador.sumarGol();
        receptor.recibirGol();
        balon.reiniciar(); // tambien limpia el poder del balon
        limpiarFalsos();

        if (estado == Estado.MUERTE_SUBITA) {
            terminar(anotador);
        } else if (modo == Modo.GOLES && anotador.getGoles() >= golesObjetivo) {
            terminar(anotador);
        }
    }

    /** Balones falsos que mantienen en juego los efectos activos de ambos jugadores (Ilusion). */
    public List<Balonfalso> getBalonesFalsos() {
        List<Balonfalso> todos = new ArrayList<>();
        for (Jugador j : new Jugador[] { jugador, jugador2 }) {
            if (j.getEfectoActivo() != null) todos.addAll(j.getEfectoActivo().getFalsos());
        }
        return todos;
    }

    private void limpiarFalsos() {
        for (Jugador j : new Jugador[] { jugador, jugador2 }) {
            if (j.getEfectoActivo() != null) j.getEfectoActivo().getFalsos().clear();
        }
    }

    public Jugador getJugador() { return jugador; }
    public Jugador getJugador2() { return jugador2; }
    public Balon getBalon() { return balon; }
    public Cancha getCanchaizq() { return canchaizq; }
    public Cancha getCanchader() { return canchader; }
    public int[] getGoles() { return new int[]{ jugador.getGoles(), jugador2.getGoles() }; }
    public Estado getEstado() { return estado; }
    public Modo getModo() { return modo; }
    public int getGolesObjetivo() { return golesObjetivo; }
    public Jugador getGanador() { return ganador; }
}