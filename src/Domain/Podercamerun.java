package Domain;

public class Podercamerun extends Poder{

	public Podercamerun() {
		super("Camerun",5);
	}

	@Override
	public double getVelocidadTiro() {

		return 22.0;
	}

	@Override
	protected Efecto crearEfecto() {

		return new efectocamerun();
	}
	

}
