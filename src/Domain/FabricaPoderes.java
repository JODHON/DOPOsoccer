package Domain;

public class FabricaPoderes {
    private FabricaPoderes() {}

    // Devuelve el poder del pais
    public static Poder crear(String pais) {
        switch (pais) {
            // case "alemania": return new PoderAlemania();
            default: return null;
        }
    }
}
