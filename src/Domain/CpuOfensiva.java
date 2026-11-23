package Domain;

/** Ofensiva: su prioridad es anotar. Persigue el balon, patea apenas puede y usa el poder en cuanto lo tiene. */
public class CpuOfensiva extends Cpu {

    private static final double DISTANCIA_DASH = 350;

    public CpuOfensiva(Partido partido) { super(partido); }

    @Override protected int periodo() { return 3; }

    @Override protected void decidir() {
        yo.usarPoder(); // no hace nada si la barra no esta llena

        double[] futuro = predecirBalon(framesHastaBalon());
        moverHacia(futuro[0], 12);
        saltarSiBalonAlto();

        if (balonAlAlcance() || rivalAlAlcance()) {
            yo.patear(balon, rival);
        } else if ((futuro[0] - centroX(yo)) * dirAtaque > DISTANCIA_DASH) {
            yo.dash(); // el dash siempre va hacia el arco rival
        }
    }
}