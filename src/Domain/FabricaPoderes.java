package Domain;

public class FabricaPoderes {
    private FabricaPoderes() {}

    public static Poder crear(String pais) {
        switch (pais) {
            // case "alemania": return new PoderAlemania();
            // case "corea":return new PoderCorea();
            default: return new PoderPrueba(); // TEMPORAL: cambiar a null cuando existan todos los poderes
        }
    }
}