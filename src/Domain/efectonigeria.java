package Domain;

public class efectonigeria extends Efecto{

	private static final double VELOCIDAD_ELEVACION = 2.0;

	private Jugador jugador;

	
	public efectonigeria() {
		super("Inconsciente",180);
	}
	
	@Override
	public void aplicar(Jugador j) {
		jugador=j;
		j.aturdir(180);
		
	}
	@Override public void quitar(Jugador j)  { }

    @Override public void actualizar(double dt) {
        super.actualizar(dt); 
        jugador.elevar(VELOCIDAD_ELEVACION * dt);
    }

}
