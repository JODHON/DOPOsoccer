package Domain;

/** Contrato para una regla concreta de colision del partido. */
public interface Colisiones {

    void verificar(Partido partido);
}
