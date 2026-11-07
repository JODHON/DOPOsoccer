package Domain;

import java.awt.Rectangle;

public class Jugador {
    private String pais;
    private int lado; // 1 izquierda, 2 derecha
    private boolean enSuelo;
    private double cargaPoder;
    private boolean poderActivo;
    private double cooldownDash;
    private double tiempoDesdePatada;
    private double tiempoSlide;
    private double animPatada;

    private Rectangle hitbox;

    private double x;
    private double y;
    private double vx;
    private double vy;
    private int ancho = 200;
    private int alto = 255;

    private int margenX = 60;
    private int margenY = 10;

    public Jugador(double xInicial, double yInicial, int lado, String pais) {
        this.x = xInicial;
        this.y = yInicial;
        this.lado = lado;
        this.pais = pais;
        this.enSuelo = true;
        actualizarHitbox();
    }

    public void actualizar(double dt) {
        vy += 0.5 * dt;
        double vxEfectiva = (tiempoSlide > 0) ? ((lado == 1) ? 12.0 : -12.0) : vx;
        x += vxEfectiva * dt;
        y += vy * dt;

        if (y >= 720 - alto) {
            y = 720 - alto;
            vy = 0;
            enSuelo = true;
        } else {
            enSuelo = false;
        }

        if (x < 0) x = 0;
        if (x > 1280 - ancho) x = 1280 - ancho;

        if (cooldownDash > 0) cooldownDash -= dt;
        if (tiempoDesdePatada > 0) tiempoDesdePatada -= dt;
        if (tiempoSlide > 0) tiempoSlide -= dt;
        if (animPatada > 0) animPatada -= dt;

        // La barra se llena en 10 s (600 frames a 60 fps)
        if (cargaPoder < 100) cargaPoder = Math.min(100, cargaPoder + (100.0 / 600) * dt);

        actualizarHitbox();
    }

    private void actualizarHitbox() {
        int hx = (int) x + margenX;
        int hy = (int) y + margenY;
        int hAncho = ancho - margenX * 2;
        int hAlto = alto - margenY * 2;

        if (hitbox == null) {
            hitbox = new Rectangle(hx, hy, hAncho, hAlto);
        } else {
            hitbox.setBounds(hx, hy, hAncho, hAlto);
        }
    }

    /** Estado actual, usado para elegir el sprite: idle, forward, jump, fall, kick, slide, action. */
    public String getEstado() {
        if (tiempoSlide > 0) return "slide";
        if (animPatada > 0) return "kick";
        if (poderActivo && enSuelo) return "action";
        if (!enSuelo) return (vy < 0) ? "jump" : "fall";
        if (vx != 0) return "forward";
        return "idle";
    }

    public void ejecutarAccion(String accion, Balon balon) {
        switch (accion) {
            case "SALTAR": saltar(); break;
            case "PATEAR": patear(balon); break;
            case "DASH": dash(); break;
            case "PODER": usarPoder(); break;
        }
    }

    public void mover(int dir) {
        // -1 izquierda, 1 derecha, 0 detener
        this.vx = dir * 5.0;
    }

    public void saltar() {
        if (enSuelo) {
            vy = -12.0;
            enSuelo = false;
        }
    }

    public void patear(Balon balon) {
        if (tiempoDesdePatada > 0) return;
        tiempoDesdePatada = 30; // frames (0.5 s a 60 fps)
        animPatada = 12;

        Rectangle alcance = new Rectangle(hitbox);
        alcance.grow(40, 20);
        if (alcance.intersects(balon.getHitbox())) {
            balon.golpear(14.0, (lado == 1) ? -Math.PI / 4 : -3 * Math.PI / 4);
        }
    }

    public void dash() {
        if (cooldownDash <= 0) {
            tiempoSlide = 20;
            cooldownDash = 120; // frames (2 s a 60 fps)
        }
    }

    public boolean usarPoder() {
        if (cargaPoder >= 100 && !poderActivo) {
            poderActivo = true;
            cargaPoder = 0;
            return true;
        }
        return false;
    }

    public boolean tienePoder() { return poderActivo; }

    public void consumirPoder() { poderActivo = false; }

    public void cargaPoder(double cant) {
        this.cargaPoder = Math.min(100, this.cargaPoder + cant);
    }

    public void reiniciarPos(double xInicial, double yInicial) {
        this.x = xInicial;
        this.y = yInicial;
        this.vx = 0;
        this.vy = 0;
        enSuelo = true;
        actualizarHitbox();
    }

    public boolean devolverPoder(Balon balon) {
        if (balon.tienePoderActivo() && balon.getLanzador() != this) {
            balon.devolverA(this, 15.0);
            return true;
        }
        return false;
    }
    
    public void moverJugador(double n) {
    	x += n;
    }

    public Rectangle getHitbox() { return hitbox; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public int getLado() { return lado; }
    public String getPais() { return pais; }
    public double getCargaPoder() { return cargaPoder; }
}