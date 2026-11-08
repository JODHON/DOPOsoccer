package Domain;

import java.awt.Rectangle;

public class Jugador {
    private static final double GANANCIA_POR_GOL_RECIBIDO = 25; // 1/4 de la barra
    private static final double FUERZA_PATADA = 14.0;  // impulso al balon (fijo; kick escala el dano)
    private static final double COOLDOWN_PATADA = 30;  // frames (0.5 s a 60 fps)
    private static final double DURACION_STUN = 180;   // frames (3 s a 60 fps)

    // Informacion del jugador
    private String pais;
    private int lado; // 1 izquierda, 2 derecha
    private int goles;
    private double vida;
    private double cargaPoder;
    private boolean poderActivo;      // aura activa
    private final Poder poder;        // tiro de poder unico del pais (null si aun no existe)
    private Efecto efectoActivo;      // maximo uno; el nuevo reemplaza al anterior
    private final Estadisticas stats = new Estadisticas();
    private int puntos;

    // Estado de movimiento
    private boolean enSuelo;
    private double cooldownDash;
    private double tiempoDesdePatada;
    private double tiempoSlide;
    private double animPatada;
    private double tiempoStun;
    private double factorVelocidad = 1.0; // modificado por efectos

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
        this.poder = FabricaPoderes.crear(pais);
        this.vida = stats.getVidaMaxima();
        this.enSuelo = true;
        actualizarHitbox();
    }

    public void actualizar(double dt) {
        vy += 0.5 * dt;
        double vxBase = (tiempoStun > 0) ? 0
                : (tiempoSlide > 0) ? ((lado == 1) ? 12.0 : -12.0) : vx * factorVelocidad;
        x += vxBase * dt;
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
        if (tiempoStun > 0) tiempoStun -= dt;

        // La barra se llena en 10 s (600 frames a 60 fps)
        if (cargaPoder < 100) cargaPoder = Math.min(100, cargaPoder + (100.0 / 600) * dt);

        if (efectoActivo != null) {
            efectoActivo.actualizar(dt);
            if (efectoActivo.haExpirado()) {
                efectoActivo.quitar(this);
                efectoActivo = null;
            }
        }

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
        if (tiempoStun > 0) return "stun";
        if (tiempoSlide > 0) return "slide";
        if (animPatada > 0) return "kick";
        if (poderActivo && enSuelo) return "action";
        if (!enSuelo) return (vy < 0) ? "jump" : "fall";
        if (vx != 0) return "forward";
        return "idle";
    }

    public void mover(int dir) {
        // -1 izquierda, 1 derecha, 0 detener
        this.vx = dir * stats.getVelocidad();
    }

    public void saltar() {
        if (enSuelo && !estaAturdido()) {
            vy = -stats.getFuerzaSalto();
            enSuelo = false;
        }
    }

    /** Patada: si toca el balon solo lo impulsa; si no, daña al rival si esta al alcance del pie. */
    public void patear(Balon balon, Jugador rival) {
        if (estaAturdido() || tiempoDesdePatada > 0) return;
        tiempoDesdePatada = COOLDOWN_PATADA;
        animPatada = 12;

        Rectangle alcance = new Rectangle(hitbox);
        alcance.grow(40, 20);
        if (alcance.intersects(balon.getHitbox())) {
            balon.golpear(FUERZA_PATADA, (lado == 1) ? -Math.PI / 4 : -3 * Math.PI / 4);
        } else if (alcance.intersects(rival.getHitbox())) {
            rival.recibirDaño(stats.getdañoPatada());
        }
    }

    public void dash() {
        if (!estaAturdido() && cooldownDash <= 0) {
            tiempoSlide = 20;
            cooldownDash = 120; // frames (2 s a 60 fps)
        }
    }

    // ---------- Poder ----------

    /** Con la barra llena y sin aura activa: vacia la barra y activa el aura. */
    public boolean usarPoder() {
        if (poder != null && !estaAturdido() && cargaPoder >= 100 && !poderActivo) {
            poderActivo = true;
            cargaPoder = 0;
            return true;
        }
        return false;
    }

    public boolean tienePoder()  { return poderActivo; }
    public void consumirPoder()  { poderActivo = false; }
    public Poder getPoder()      { return poder; }

    public void cargaPoder(double cant) {
        this.cargaPoder = Math.min(100, this.cargaPoder + cant);
    }

    /** Cuando el rival anota, la barra sube 1/4 de inmediato. */
    public void recibirGol() { cargaPoder(GANANCIA_POR_GOL_RECIBIDO); }

    // ---------- Vida y efectos ----------

    /** Si la vida llega a 0: stun de 3 s y la vida vuelve al maximo. */
    public void recibirDaño(double daño) {
        vida = Math.max(0, vida - daño);
        if (vida <= 0) {
            tiempoStun = DURACION_STUN;
            tiempoSlide = 0;
            animPatada = 0;
            vida = stats.getVidaMaxima();
        }
    }

    public boolean estaAturdido() { return tiempoStun > 0; }

    /** El nuevo efecto reemplaza al anterior (no se acumulan). */
    public void aplicarEfecto(Efecto nuevo) {
        if (efectoActivo != null) efectoActivo.quitar(this);
        efectoActivo = nuevo;
        if (nuevo != null) nuevo.aplicar(this);
    }

    public void setFactorVelocidad(double f) { this.factorVelocidad = f; }

    // ---------- Goles y puntos ----------

    public void sumarGol()           { goles++; }
    public void sumarPuntos(int p)   { puntos += p; }

    /** Compra un punto de mejora si alcanzan los puntos y no esta al maximo. */
    public boolean comprarMejora(Estadisticas.Stat stat) {
        if (stats.estaAlMaximo(stat)) return false;
        int costo = stats.getCostoSiguiente(stat);
        if (puntos < costo) return false;
        puntos -= costo;
        stats.subir(stat);
        if (stat == Estadisticas.Stat.SURVIVAL) vida = stats.getVidaMaxima();
        return true;
    }

    public void reiniciarPos(double xInicial, double yInicial) {
        this.x = xInicial;
        this.y = yInicial;
        this.vx = 0;
        this.vy = 0;
        enSuelo = true;
        actualizarHitbox();
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
    public int getGoles() { return goles; }
    public double getVida() { return vida; }
    public double getVidaMaxima() { return stats.getVidaMaxima(); }
    public int getPuntos() { return puntos; }
    public Estadisticas getStats() { return stats; }
    public Efecto getEfectoActivo() { return efectoActivo; }
}