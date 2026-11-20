package Domain;

public class DopoSoccerException extends Exception {

    public static final String PAIS_NULO="El país no puede ser nulo ni vacío";
    public static final String MODO_NULO="El modo de juego no puede ser nulo";
    public static final String LIMITE_INVALIDO="El límite debe ser mayor que cero";
    public static final String STAT_NULA="La estadística no puede ser nula";
    public static final String STAT_AL_MAXIMO="La estadística ya está en el nivel máximo";
    public static final String PUNTOS_INSUFICIENTES="Puntos insuficientes para la mejora";

    public DopoSoccerException(String mensaje) {
        super(mensaje);
    }

    public DopoSoccerException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
