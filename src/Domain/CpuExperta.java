package Domain;

/**
 * Experta: decide segun el estado del partido (marcador, tiempo, modo, muerte subita,
 * rival aturdido) si ataca, defiende o juega equilibrado, y reacciona cada ciclo.
 */
public class CpuExperta extends Cpu {

    private enum Postura { ATAQUE, DEFENSA, EQUILIBRADO }

    private static final double SEGUNDOS_FINALES = 20;
    private static final double MARGEN_CENTRO = 40;
    private static final double DISTANCIA_DASH = 300;
    private static final double DISTANCIA_PODER = 500;

    public CpuExperta(Partido partido) { super(partido); }

    @Override protected int periodo() { return 1; }

    @Override protected void decidir() {
        Postura postura = evaluarPostura();
        double[] futuro = predecirBalon(framesHastaBalon());

        gestionarPoder();

        switch (postura) {
            case ATAQUE: atacar(futuro); break;
            case DEFENSA: defender(futuro); break;
            default:
                if (enMitadPropia(futuro[0])) defender(futuro);
                else atacar(futuro);
        }

        if (balonAlAlcance() || (rivalAlAlcance() && postura != Postura.DEFENSA)) {
            yo.patear(balon, rival);
        }
    }

    /** Lee el estado del partido y elige la postura. */
    private Postura evaluarPostura() {
        if (rival.estaAturdido()) return Postura.ATAQUE; // rival indefenso: aprovecha

        int mios = yo.getGoles();
        int suyos = rival.getGoles();

        if (partido.getEstado() == Partido.Estado.MUERTE_SUBITA) return Postura.EQUILIBRADO;

        if (partido.getModo() == Partido.Modo.GOLES) {
            int matchPoint = partido.getGolesObjetivo() - 1;
            if (suyos >= matchPoint && mios < matchPoint) return Postura.DEFENSA; // un gol y pierde
            if (mios >= matchPoint && suyos < matchPoint) return Postura.ATAQUE;  // un gol y gana
            if (mios <= suyos - 2) return Postura.ATAQUE;
            return Postura.EQUILIBRADO;
        }

        // Modo TIEMPO
        if (partido.getTiempoRestante() <= SEGUNDOS_FINALES) {
            return (mios > suyos) ? Postura.DEFENSA : Postura.ATAQUE; // cuidar ventaja / buscar gol
        }
        return (mios <= suyos - 2) ? Postura.ATAQUE : Postura.EQUILIBRADO;
    }

    private void gestionarPoder() {
        if (yo.tienePoder()) return;
        boolean balonCerca = Math.abs(centroBalonX() - centroX(yo)) < DISTANCIA_PODER;
        if (balonCerca || rival.estaAturdido()) yo.usarPoder();
    }

    private void atacar(double[] futuro) {
        moverHacia(futuro[0], 12);
        saltarSiBalonAlto();
        if (!balonAlAlcance() && (futuro[0] - centroX(yo)) * dirAtaque > DISTANCIA_DASH) yo.dash();
    }

    private void defender(double[] futuro) {
        boolean peligro = enMitadPropia(futuro[0]) || balonVaHaciaMiArco();
        double objetivo = peligro
                ? limitarAMitadPropia(futuro[0], MARGEN_CENTRO)
                : posicionDeGuardia();
        moverHacia(objetivo, 15);
        saltarSiBalonAlto();
    }
}