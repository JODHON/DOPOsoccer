package Domain;

import java.awt.event.KeyEvent;

/**
 * Coordinador principal de la partida: recibe entradas, actualiza entidades
 * y solicita la comprobación de colisiones y del reloj.
 */
public class DOPOsoccer {

    private final Partido partido;
    private final GestorColisiones gestorColisiones;
    private final Cpu cpu; // null en modo 1 vs 1

    private boolean izquierda1;
    private boolean derecha1;
    private boolean izquierda2;
    private boolean derecha2;

    public DOPOsoccer(String pais1, String pais2) {
        this(pais1, pais2, Partido.Modo.TIEMPO, Partido.DURACION_DEFECTO_S);
    }

    public DOPOsoccer(String pais1, String pais2, Partido.Modo modo, int limite) {
        this(pais1, pais2, modo, limite, null);
    }

    /** dificultad == null: 1 vs 1. Si no, el jugador 2 lo controla la CPU. */
    public DOPOsoccer(String pais1, String pais2, Partido.Modo modo, int limite, Dificultad dificultad) {
        this(pais1, pais2, modo, limite, dificultad, null);
    }

    /** anterior: partida previa cuyos puntos y mejoras se conservan (puede ser null). */
    public DOPOsoccer(String pais1, String pais2, Partido.Modo modo, int limite,
                      Dificultad dificultad, DOPOsoccer anterior) {
        try {
            partido = new Partido(pais1, pais2, modo, limite,
                    anterior == null ? null : anterior.partido);
            cpu = (dificultad == null) ? null : FabricaCpu.crear(dificultad, partido);
        }
        catch (DopoSoccerException e) {
            throw new RuntimeException(e);
        }
        gestorColisiones = new GestorColisiones();
    }

    /** Ejecuta un ciclo de juego. La GUI lo llama desde su Timer. */
    public void actualizarJuego() {
        if (partido.getEstado() == Partido.Estado.TERMINADO || partido.isPausado()) return;

        if (cpu != null) cpu.actualizar();

        partido.getJugador().actualizar(1.0);
        partido.getJugador2().actualizar(1.0);
        partido.getBalon().actualizar(1.0);

        gestorColisiones.verificar(partido);
        partido.comprobarFinPorTiempo();
    }

    // TECLADO 

    public void teclaPresionada(int codigo) {
        if (cpu != null && esTeclaJugador2(codigo)) return; // la CPU controla al jugador 2

        switch (codigo) {
            case KeyEvent.VK_A: izquierda1 = true; break;
            case KeyEvent.VK_D: derecha1 = true; break;
            case KeyEvent.VK_LEFT: izquierda2 = true; break;
            case KeyEvent.VK_RIGHT: derecha2 = true; break;
        }

        if (!partido.isPausado() && partido.getEstado() != Partido.Estado.TERMINADO) {
            Jugador jugador1 = partido.getJugador();
            Jugador jugador2 = partido.getJugador2();
            Balon balon = partido.getBalon();

            switch (codigo) {
                case KeyEvent.VK_W: jugador1.saltar(); break;
                case KeyEvent.VK_F: jugador1.patear(balon, jugador2); break;
                case KeyEvent.VK_G: jugador1.dash(); break;
                case KeyEvent.VK_H: jugador1.usarPoder(); break;

                case KeyEvent.VK_UP: jugador2.saltar(); break;
                case KeyEvent.VK_COMMA: jugador2.patear(balon, jugador1); break;
                case KeyEvent.VK_PERIOD: jugador2.dash(); break;
                case KeyEvent.VK_SLASH: jugador2.usarPoder(); break;
            }
        }
        actualizarMovimiento();
    }

    public void teclaLiberada(int codigo) {
        switch (codigo) {
            case KeyEvent.VK_A: izquierda1 = false; break;
            case KeyEvent.VK_D: derecha1 = false; break;
            case KeyEvent.VK_LEFT: izquierda2 = false; break;
            case KeyEvent.VK_RIGHT: derecha2 = false; break;
        }
        actualizarMovimiento();
    }

    private void actualizarMovimiento() {
        partido.getJugador().mover((derecha1 ? 1 : 0) - (izquierda1 ? 1 : 0));
        if (cpu == null) {
            partido.getJugador2().mover((derecha2 ? 1 : 0) - (izquierda2 ? 1 : 0));
        }
    }

    private boolean esTeclaJugador2(int codigo) {
        return codigo == KeyEvent.VK_LEFT || codigo == KeyEvent.VK_RIGHT
                || codigo == KeyEvent.VK_UP || codigo == KeyEvent.VK_COMMA
                || codigo == KeyEvent.VK_PERIOD || codigo == KeyEvent.VK_SLASH;
    }

    // PAUSA 

    public void pausar() { partido.pausar(); }
    public void reanudar() { partido.reanudar(); }
    public boolean isPausado() { return partido.isPausado(); }
    public boolean isModoCpu() { return cpu != null; }

    // CONSULTAS PARA LA GUI 

    public int[] getGoles() { return partido.getGoles(); }
    public Partido.Estado getEstado() { return partido.getEstado(); }
    public Partido.Modo getModo() { return partido.getModo(); }
    public int getGolesObjetivo() { return partido.getGolesObjetivo(); }
    public double getTiempoRestante() { return partido.getTiempoRestante(); }
    public Jugador getGanador() { return partido.getGanador(); }
    public Jugador getJugador() { return partido.getJugador(); }
    public Jugador getJugador2() { return partido.getJugador2(); }
    public Balon getBalon() { return partido.getBalon(); }
    public Cancha getCanchaizq() { return partido.getCanchaizq(); }
    public Cancha getCanchader() { return partido.getCanchader(); }
    public java.util.List<Balonfalso> getBalonesFalsos() { return partido.getBalonesFalsos(); }
}