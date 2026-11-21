package Domain;

/** Separa a los jugadores cuando sus hitboxes se superponen. */
public final class ColisionJugadorJugador implements Colisiones {

    @Override
    public void verificar(Partido partido) {
        Jugador jugador1 = partido.getJugador();
        Jugador jugador2 = partido.getJugador2();

        if (jugador1.getHitbox().intersects(jugador2.getHitbox())) {
            jugador1.moverJugador(-1.0);
            jugador2.moverJugador(1.0);
        }
    }
}
