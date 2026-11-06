package Domain;

import java.awt.Rectangle;

public class Cancha {
    private double ancho;
    private double altura;
    private double x;
    private double y;
    
    // Colisiones
    private Rectangle techo;
    private Rectangle red;

    public Cancha(double ancho, double altura, double x, double y) {
        this.ancho = ancho;
        this.altura = altura;
        this.x = x;
        this.y = y;
        
        
        this.techo = new Rectangle((int)x, (int)y, (int)ancho, 10); 
        this.red = new Rectangle((int)(x + ancho / 2), (int)y, 5, (int)altura); 
    }
    
    public double getAncho() { return ancho; }
    public double getAltura() { return altura; }
    public double getX() { return x; }
    public double getY() { return y; }
    
    public Rectangle getTecho() { return techo; }
    public Rectangle getRed() { return red; }
}