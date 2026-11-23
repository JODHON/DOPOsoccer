package Domain;

import java.util.Random;

/** Torpe: elige al azar sus movimientos y acciones, sin mirar el partido. */
public class CpuTorpe extends Cpu {

    private final Random azar = new Random();

    public CpuTorpe(Partido partido) { super(partido); }

    @Override protected int periodo() { return 25; }

    @Override protected void decidir() {
        yo.mover(azar.nextInt(3) - 1); // -1, 0 o 1
        switch (azar.nextInt(10)) {
            case 0: yo.saltar(); break;
            case 1: yo.patear(balon, rival); break;
            case 2: yo.dash(); break;
            case 3: yo.usarPoder(); break;
            default: break;
        }
    }
}