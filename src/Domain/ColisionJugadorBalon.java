package Domain;

/** Resuelve los contactos entre los jugadores y el balon. */
public final class ColisionJugadorBalon implements Colisiones {

    @Override
    public void verificar(Partido partido) {
        Jugador jugador1 = partido.getJugador();
        Jugador jugador2 = partido.getJugador2();
        Balon balon = partido.getBalon();

        impactoPoder(jugador1, balon);
        impactoPoder(jugador2, balon);

        colisionJugador(jugador1, balon);
        colisionJugador(jugador2, balon);
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
}
