package Domain;

public class Podernigeria extends Poder{

	public Podernigeria() {
		super("Nigeria", 8);
	}

	@Override
	public double getVelocidadTiro() {
		// TODO Auto-generated method stub
		return 22.0;
	}

	@Override
	protected Efecto crearEfecto() {

		return new efectonigeria();
	}

}
