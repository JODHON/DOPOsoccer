package Domain;

public class Podercamerun extends Poder{

	public Podercamerun() {
		super("Camerun", 12); // 12 x 5 = 60 de dano real (Anexo 1)
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