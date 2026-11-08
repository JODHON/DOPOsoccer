package Domain;

import java.awt.Rectangle;

public class Balon {
    private double rebote;
    private double xInicial;
    private double yInicial;
    private int size;
    private Poder poderActivo;
    private Jugador lanzador;

    private Rectangle hitbox;

    private double x;
    private double y;
    private double vx;
    private double vy;

    public Balon(double xInicial, double yInicial, int size, double rebote) {
        this.xInicial = xInicial;
        this.yInicial = yInicial;
        this.size = size;
        this.rebote = rebote;
        reiniciar();
    }

    public void actualizar(double dt) {
        vy += 0.4 * dt;
        x += vx * dt;
        y += vy * dt;

        if (y > 720 - size) {
            y = 720 - size;
            vy = -vy * rebote;
        }

        if (x < 0 || x > 1280 - size) {
            vx = -vx * rebote;
            if (x < 0) x = 0;
            if (x > 1280 - size) x = 1280 - size;
        }

        actualizarHitbox();
    }

    public void golpear(double fuerza, double angulo) {
        this.vx = fuerza * Math.cos(angulo);
        this.vy = fuerza * Math.sin(angulo);
    }

    public void rebotarVertical(double nuevaY) {
        this.y = nuevaY;
        this.vy = -this.vy * rebote;
        actualizarHitbox();
    }

    public void reiniciar() {
        this.x = xInicial;
        this.y = yInicial;
        this.vx = 0;
        this.vy = 0;
        this.poderActivo = null;
        this.lanzador = null;
        actualizarHitbox();
    }

    public boolean tienePoderActivo() { return poderActivo != null; }
    public Poder getPoderActivo()     { return poderActivo; }
    public Jugador getLanzador()      { return lanzador; }

    public void activarPoder(Poder poder, Jugador lanzador) {
        this.poderActivo = poder;
        this.lanzador = lanzador;
    }

    public void limpiarPoder() {
        this.poderActivo = null;
        this.lanzador = null;
    }

    private void actualizarHitbox() {
        if (hitbox == null) {
            hitbox = new Rectangle((int) x, (int) y, size, size);
        } else {
            hitbox.setBounds((int) x, (int) y, size, size);
        }
    }

    public Rectangle getHitbox() { return hitbox; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
}