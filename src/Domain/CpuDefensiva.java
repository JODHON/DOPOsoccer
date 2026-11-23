package Domain;

/** Defensiva: su prioridad es proteger el arco. Espera delante de el y solo sale cuando hay peligro. */
public class CpuDefensiva extends Cpu {

    private static final double MARGEN_CENTRO = 40;
    private static final double VELOCIDAD_PELIGRO = 8;

    public CpuDefensiva(Partido partido) { super(partido); }

    @Override protected int periodo() { return 4; }

    @Override protected void decidir() {
        double bx = centroBalonX();
        boolean peligro = enMitadPropia(bx)
                || (balonVaHaciaMiArco() && Math.abs(balon.getVx()) > VELOCIDAD_PELIGRO);

        if (!peligro) {
            moverHacia(posicionDeGuardia(), 25);
            return;
        }

        yo.usarPoder(); // despeje con poder
        double[] futuro = predecirBalon(framesHastaBalon());
        moverHacia(limitarAMitadPropia(futuro[0], MARGEN_CENTRO), 15);
        saltarSiBalonAlto();
        if (balonAlAlcance() || rivalAlAlcance()) yo.patear(balon, rival);
    }
}