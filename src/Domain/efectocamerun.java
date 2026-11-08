package Domain;


public class efectocamerun extends Efecto{

	public efectocamerun() { super("Electrocutado", 180); }

	@Override
	public void aplicar(Jugador j) {
		j.setSaltoBloqueado(true);
		j.setFactorVelocidad(0.5);
	}

	@Override
	public void quitar(Jugador j) {
        j.setSaltoBloqueado(false);
        j.setFactorVelocidad(1.0);
		
	}
	

}
