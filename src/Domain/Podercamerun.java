package Domain;

public class Podercamerun extends Poder{

	protected Podercamerun() {
		super("Corea",5);
	}

	@Override
	public double getVelocidadTiro() {
		// TODO Auto-generated method stub
		return 22.0;
	}

	@Override
	protected Efecto crearEfecto() {
		// TODO Auto-generated method stub
		return new efectocamerun();
	}
	

}
