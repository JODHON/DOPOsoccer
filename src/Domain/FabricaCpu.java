package Domain;

public final class FabricaCpu {
    private FabricaCpu() {}

    public static Cpu crear(Dificultad dificultad, Partido partido) throws DopoSoccerException {
        if (dificultad == null) throw new DopoSoccerException(DopoSoccerException.DIFICULTAD_NULA);
        switch (dificultad) {
            case TORPE: return new CpuTorpe(partido);
            case OFENSIVA: return new CpuOfensiva(partido);
            case DEFENSIVA: return new CpuDefensiva(partido);
            default: return new CpuExperta(partido);
        }
    }
}
