package Domain;

public class FabricaPoderes {
    private FabricaPoderes() {}

    public static Poder crear(String pais) {
        switch (pais) {
        	case "eeuu": return new Podereeeuu();
        	case "corea":return new Podercorea();
        	case "camerun":return new Podercamerun();
        	case "nigeria":return new Podernigeria();
            default: return new PoderPrueba(); 
        }
    }
}