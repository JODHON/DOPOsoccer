package Domain;

import java.awt.Rectangle;

/** Resuelve el rebote del balon contra los techos de las canchas. */
public final class ColisionTecho implements Colisiones {

    @Override
    public void verificar(Partido partido) {
        Balon balon = partido.getBalon();
        colisionar(partido.getCanchaizq(), balon);
        colisionar(partido.getCanchader(), balon);
    }

    private void colisionar(Cancha cancha, Balon balon) {
        Rectangle techo = cancha.getTecho();
        if (!balon.getHitbox().intersects(techo)) return;

        double nuevaY = balon.getHitbox().getCenterY() < techo.getCenterY()
                ? techo.y - balon.getSize()
                : techo.y + techo.height;
        balon.rebotarVertical(nuevaY);
    }
}
