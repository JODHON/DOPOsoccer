package Domain;

/** Registra un gol cuando el balon atraviesa una de las redes. */
public final class ColisionRed implements Colisiones {

    @Override
    public void verificar(Partido partido) {
        Balon balon = partido.getBalon();

        // Si el balon esta tocando un techo, no se registra un gol en este ciclo.
        if (tocaTecho(partido.getCanchaizq(), balon)
                || tocaTecho(partido.getCanchader(), balon)) {
            return;
        }

        if (balon.getHitbox().intersects(partido.getCanchaizq().getRed())) {
            partido.registrarGol(partido.getJugador2(), partido.getJugador());
        } else if (balon.getHitbox().intersects(partido.getCanchader().getRed())) {
            partido.registrarGol(partido.getJugador(), partido.getJugador2());
        }
    }

    private boolean tocaTecho(Cancha cancha, Balon balon) {
        return balon.getHitbox().intersects(cancha.getTecho());
    }
}
