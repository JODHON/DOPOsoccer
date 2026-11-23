package Domain;

import java.io.Serializable;

import java.awt.Rectangle;

/**
 * Base de los controladores de la CPU. Maneja a un jugador con las mismas acciones
 * que un humano (mover, saltar, patear, dash, poder); cada dificultad solo cambia la decision.
 */
public abstract class Cpu implements Serializable {

    private static final double GRAVEDAD_BALON = 0.4;
    private static final double REBOTE_BALON = 0.75; // igual al de Partido
    private static final double ANCHO_CAMPO = 1280;
    private static final double SUELO = 720;
    private static final double CENTRO = 640;
    private static final int MAX_FRAMES_PREDICCION = 40;

    protected final Partido partido;
    protected final Jugador yo;
    protected final Jugador rival;
    protected final Balon balon;
    protected final Cancha arcoPropio;
    /** +1 si ataca hacia la derecha (lado 1), -1 si ataca hacia la izquierda (lado 2). */
    protected final int dirAtaque;

    private int espera;

    protected Cpu(Partido partido) {
        this.partido = partido;
        this.yo = partido.getJugador2();
        this.rival = partido.getJugador();
        this.balon = partido.getBalon();
        this.dirAtaque = (yo.getLado() == 1) ? 1 : -1;
        this.arcoPropio = (yo.getLado() == 1) ? partido.getCanchaizq() : partido.getCanchader();
    }

    /** Se llama una vez por ciclo de juego; decide solo cada periodo() ciclos. */
    public final void actualizar() {
        if (espera > 0) { espera--; return; }
        espera = Math.max(1, periodo()) - 1;
        decidir();
    }

    /** Ciclos entre decisiones: mas alto = reaccion mas lenta. */
    protected abstract int periodo();

    protected abstract void decidir();

    // ---------------- Utilidades de lectura ----------------

    protected double centroX(Jugador j) { return j.getHitbox().getCenterX(); }

    protected double centroBalonX() { return balon.getHitbox().getCenterX(); }

    protected double frenteArcoPropio() {
        return (yo.getLado() == 1) ? arcoPropio.getX() + arcoPropio.getAncho() : arcoPropio.getX();
    }

    /** Punto de espera delante del arco propio. */
    protected double posicionDeGuardia() { return frenteArcoPropio() + dirAtaque * 120; }

    protected boolean enMitadPropia(double x) { return (x - CENTRO) * dirAtaque < 0; }

    protected boolean balonVaHaciaMiArco() { return balon.getVx() * dirAtaque < -1; }

    /** Limita x a la mitad propia de la cancha (con margen). */
    protected double limitarAMitadPropia(double x, double margen) {
        return (dirAtaque == 1) ? Math.min(x, CENTRO - margen) : Math.max(x, CENTRO + margen);
    }

    /** Misma zona de alcance que usa Jugador.patear. */
    private Rectangle alcance() {
        Rectangle a = new Rectangle(yo.getHitbox());
        a.grow(40, 20);
        return a;
    }

    protected boolean balonAlAlcance() { return alcance().intersects(balon.getHitbox()); }

    protected boolean rivalAlAlcance() { return alcance().intersects(rival.getHitbox()); }

    /** Centro (x, y) del balon dentro de n frames, simulando gravedad, suelo y paredes. */
    protected double[] predecirBalon(int frames) {
        int size = balon.getSize();
        double x = balon.getX(), y = balon.getY(), vx = balon.getVx(), vy = balon.getVy();
        for (int i = 0; i < frames; i++) {
            vy += GRAVEDAD_BALON;
            x += vx;
            y += vy;
            if (y > SUELO - size) { y = SUELO - size; vy = -vy * REBOTE_BALON; }
            if (x < 0 || x > ANCHO_CAMPO - size) {
                vx = -vx * REBOTE_BALON;
                x = Math.max(0, Math.min(ANCHO_CAMPO - size, x));
            }
        }
        return new double[]{x + size / 2.0, y + size / 2.0};
    }

    /** Frames que tardaria la CPU en llegar al balon actual (acotado). */
    protected int framesHastaBalon() {
        double dist = Math.abs(centroBalonX() - centroX(yo));
        return (int) Math.min(MAX_FRAMES_PREDICCION, dist / Math.max(1, yo.getStats().getVelocidad()));
    }

    // ---------------- Utilidades de accion ----------------

    protected void moverHacia(double objetivoX, double tolerancia) {
        double dif = objetivoX - centroX(yo);
        if (Math.abs(dif) <= tolerancia) yo.mover(0);
        else yo.mover(dif > 0 ? 1 : -1);
    }

    /** Salta si el balon esta por encima de la cabeza y cerca en horizontal. */
    protected void saltarSiBalonAlto() {
        boolean alto = balon.getHitbox().getCenterY() < yo.getHitbox().getY() + 40;
        boolean cerca = Math.abs(centroBalonX() - centroX(yo)) < 160;
        if (alto && cerca) yo.saltar();
    }
}