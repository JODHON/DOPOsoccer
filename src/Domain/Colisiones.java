package Domain;

import java.awt.Rectangle;

/** Resuelve los contactos entre jugadores, balón, techos y redes. */
public class Colisiones {

    public void verificar(Partido partido) {
        Jugador jugador1 = partido.getJugador();
        Jugador jugador2 = partido.getJugador2();
        Balon balon = partido.getBalon();

        impactoPoder(jugador1, balon);
        impactoPoder(jugador2, balon);

        colisionJugador(jugador1, balon);
        colisionJugador(jugador2, balon);

        if (jugador1.getHitbox().intersects(jugador2.getHitbox())) {
            jugador1.moverJugador(-1.0);
            jugador2.moverJugador(1.0);
        }

        boolean tocaTecho = colisionTecho(partido.getCanchaizq(), balon)
                | colisionTecho(partido.getCanchader(), balon);

        if (!tocaTecho) {
            if (balon.getHitbox().intersects(partido.getCanchaizq().getRed())) {
                partido.registrarGol(jugador2, jugador1);
            } else if (balon.getHitbox().intersects(partido.getCanchader().getRed())) {
                partido.registrarGol(jugador1, jugador2);
            }
        }
    }

    private void impactoPoder(Jugador rival, Balon balon) {
        if (!balon.tienePoderActivo() || balon.getLanzador() == rival) return;
        if (!balon.getHitbox().intersects(rival.getHitbox())) return;

        balon.getPoderActivo().aplicarImpacto(rival, balon.getLanzador(), balon);
        balon.limpiarPoder();
    }

    private void colisionJugador(Jugador jugador, Balon balon) {
        if (!jugador.getHitbox().intersects(balon.getHitbox())) return;
        if (balon.tienePoderActivo() && balon.getLanzador() == jugador) return;

        boolean haciaDerecha = balon.getHitbox().getCenterX()
                >= jugador.getHitbox().getCenterX();

        if (jugador.tienePoder()) {
            jugador.consumirPoder();
            jugador.getPoder().lanzar(balon, jugador, haciaDerecha);
        } else {
            double angulo = haciaDerecha ? -Math.PI / 4 : -3 * Math.PI / 4;
            balon.golpear(Math.hypot(6, 6), angulo);
        }
    }

    private boolean colisionTecho(Cancha cancha, Balon balon) {
        Rectangle techo = cancha.getTecho();
        if (!balon.getHitbox().intersects(techo)) return false;

        double nuevaY = balon.getHitbox().getCenterY() < techo.getCenterY()
                ? techo.y - balon.getSize()
                : techo.y + techo.height;
        balon.rebotarVertical(nuevaY);
        return true;
    }
}
