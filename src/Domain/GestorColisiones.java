package Domain;

import java.io.Serializable;

/** Coordina las distintas reglas de colision del partido. */
public final class GestorColisiones implements Serializable {

    private final Colisiones[] colisiones = {
            new ColisionJugadorBalon(),
            new ColisionJugadorJugador(),
            new ColisionRed(),
            new ColisionTecho()
    };

    public void verificar(Partido partido) {
        for (Colisiones colision : colisiones) {
            colision.verificar(partido);
        }
    }
}
