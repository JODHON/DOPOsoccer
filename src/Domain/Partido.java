package Domain;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;

public class Partido {

    private Jugador jugador;   // Jugador 1: A, D, W | F patear, G slide, H poder
    private Jugador jugador2;  // Jugador 2: flechas | , patear, . slide, / poder
    private Balon balon;
    private Cancha canchaizq;
    private Cancha canchader;

    private int[] goles; // [0] = izquierda, [1] = derecha

    private boolean izq1, der1, izq2, der2;

    public Partido(String pais1, String pais2) {
        this.jugador = new Jugador(200, 300, 1, pais1);
        this.jugador2 = new Jugador(880, 300, 2, pais2);
        this.balon = new Balon(600, 150, 80, 0.75);
        this.canchaizq = new Cancha(200, 400, 0, 315);
        this.canchader = new Cancha(200, 400, 1080, 315);

        this.goles = new int[2];
    }
    public void actualizarLogica() {
        jugador.actualizar(1.0);
        jugador2.actualizar(1.0);
        balon.actualizar(1.0);
        verificarColisiones();
    }

    public void teclaPresionada(int codigo) {
        switch (codigo) {
            case KeyEvent.VK_A: izq1 = true; break;
            case KeyEvent.VK_D: der1 = true; break;
            case KeyEvent.VK_W: jugador.saltar(); break;
            case KeyEvent.VK_F: jugador.patear(balon); break;
            case KeyEvent.VK_G: jugador.dash(); break;
            case KeyEvent.VK_H: jugador.usarPoder(); break;

            case KeyEvent.VK_LEFT: izq2 = true; break;
            case KeyEvent.VK_RIGHT: der2 = true; break;
            case KeyEvent.VK_UP: jugador2.saltar(); break;
            case KeyEvent.VK_CONTROL: jugador2.patear(balon); break;
            case KeyEvent.VK_PERIOD: jugador2.dash(); break;
            case KeyEvent.VK_SLASH: jugador2.usarPoder(); break;
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

    private void verificarColisiones() {
        colisionJugador(jugador);
        colisionJugador(jugador2);

        // Techos: rebota y no cuenta gol
        if (colisionTecho(canchaizq) || colisionTecho(canchader)) return;

        
        // entre jugadores
        if(jugador.getHitbox().intersects(jugador2.getHitbox())) {
        	jugador.moverJugador(-1.0);
        }
        if(jugador2.getHitbox().intersects(jugador.getHitbox())) {
        	jugador2.moverJugador(1.0);
        }
        // Goles
        if (balon.getHitbox().intersects(canchaizq.getRed())) {
            goles[1]++;
            balon.reiniciar();
        } else if (balon.getHitbox().intersects(canchader.getRed())) {
            goles[0]++;
            balon.reiniciar();
        }
    }

    private void colisionJugador(Jugador j) {
        if (!j.getHitbox().intersects(balon.getHitbox())) return;

        boolean haciaDerecha = balon.getHitbox().getCenterX() >= j.getHitbox().getCenterX();

        if (j.tienePoder()) {
            // Tiro de poder: sale recto y rápido, el aura desaparece
            j.consumirPoder();
            balon.golpear(22.0, haciaDerecha ? 0 : Math.PI);
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

    public Jugador getJugador() { return jugador; }
    public Jugador getJugador2() { return jugador2; }
    public Balon getBalon() { return balon; }
    public Cancha getCanchaizq() { return canchaizq; }
    public Cancha getCanchader() { return canchader; }
    public int[] getGoles() { return goles; }
}