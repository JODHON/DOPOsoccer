package Domain;

/** Niveles de dificultad de la CPU. */
public enum Dificultad {
    TORPE("Torpe"),
    OFENSIVA("Ofensiva"),
    DEFENSIVA("Defensiva"),
    EXPERTA("Experta");

    private final String etiqueta;

    Dificultad(String etiqueta) { this.etiqueta = etiqueta; }

    @Override public String toString() { return etiqueta; }
}