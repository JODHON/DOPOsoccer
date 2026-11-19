package Domain;

public class FabricaPoderes {
    private FabricaPoderes() {}

    public static Poder crear(String pais) throws DopoSoccerException {
        if (pais == null || pais.trim().isEmpty()) throw new DopoSoccerException(DopoSoccerException.PAIS_NULO);
        switch (pais.trim().toLowerCase()) {
            case "usa":
            case "eeuu": return new Podereeeuu();
            case "corea": return new Podercorea();
            case "camerun": return new Podercamerun();
            case "nigeria": return new Podernigeria();
            default: return new PoderPrueba();
    }
}
}