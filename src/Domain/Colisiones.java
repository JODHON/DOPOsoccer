package Domain;

/** Contrato para una regla concreta de colision del partido. */
public interface Colisiones extends java.io.Serializable {

    void verificar(Partido partido);
}
