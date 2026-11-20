package Presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import Domain.DOPOsoccer;
import Domain.Jugador;
import Domain.Partido;

public class HudRenderer {
    private static final Color SELECTED_COLOR = new Color(255, 200, 40);
    
    
    
    public void draw(Graphics2D graphics, JPanel panel, DOPOsoccer game, boolean cpuMode, Image blockedIcon) {
        drawScoreboard(graphics, panel, game, cpuMode);
        drawMatchClock(graphics, panel, game);
        drawPowerBar(graphics, panel, game.getJugador(), 30, 30, new Color(33, 150, 243));
        drawPowerBar(graphics, panel, game.getJugador2(), panel.getWidth() - 270, 30,
            new Color(244, 67, 54));
        
        
        drawLifeBar(graphics, panel, game.getJugador(), 30, 60, new Color(33, 150, 243));
        drawLifeBar(graphics, panel, game.getJugador2(), panel.getWidth() - 270, 60,
                new Color(244, 67, 54));
    
        
        

		drawBloqueo(graphics, panel, game.getJugador(), 276, 52, blockedIcon);
		drawBloqueo(graphics, panel, game.getJugador2(), panel.getWidth() - 340, 52, blockedIcon);
    }

    private void drawScoreboard(Graphics2D graphics, JPanel panel, DOPOsoccer game, boolean cpuMode) {
        int[] goals = game.getGoles();
        int width = 320;
        int height = 76;
        int x = (panel.getWidth() - width) / 2;
        int y = 15;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setColor(new Color(0, 0, 0, 90));
        graphics.fillRoundRect(x + 4, y + 5, width, height, 30, 30);
        graphics.setPaint(new GradientPaint(0, y, new Color(35, 35, 55, 235),
            0, y + height, new Color(10, 10, 22, 235)));
        graphics.fillRoundRect(x, y, width, height, 30, 30);

        Shape originalClip = graphics.getClip();
        graphics.setClip(new RoundRectangle2D.Float(x, y, width, height, 30, 30));
        graphics.setColor(new Color(33, 150, 243));
        graphics.fillRect(x, y, 10, height);
        graphics.setColor(new Color(244, 67, 54));
        graphics.fillRect(x + width - 10, y, 10, height);
        graphics.setClip(originalClip);

        graphics.setColor(new Color(255, 255, 255, 120));
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRoundRect(x, y, width, height, 30, 30);
        graphics.setStroke(new BasicStroke(1f));

        graphics.setFont(new Font("Arial", Font.BOLD, 14));
        graphics.setColor(new Color(180, 200, 255));
        drawCenteredText(graphics, "JUGADOR 1", x + width / 4, y + 22);
        graphics.setColor(new Color(255, 190, 185));
        drawCenteredText(graphics, cpuMode ? "CPU" : "JUGADOR 2", x + 3 * width / 4, y + 22);

        graphics.setFont(new Font("Arial", Font.BOLD, 42));
        graphics.setColor(Color.WHITE);
        drawCenteredText(graphics, String.valueOf(goals[0]), x + width / 4, y + 64);
        drawCenteredText(graphics, "-", x + width / 2, y + 60);
        drawCenteredText(graphics, String.valueOf(goals[1]), x + 3 * width / 4, y + 64);
    }

    private void drawMatchClock(Graphics2D graphics, JPanel panel, DOPOsoccer game) {
        boolean suddenDeath = game.getEstado() == Partido.Estado.MUERTE_SUBITA;
        int seconds = (int) Math.ceil(game.getTiempoRestante());
        boolean goalMode = game.getModo() == Partido.Modo.GOLES;
        String text;
        if (goalMode) text = "PRIMERO A " + game.getGolesObjetivo() + " GOLES";
        else if (suddenDeath) text = "MUERTE SÚBITA";
        else text = String.format("%d:%02d", seconds / 60, seconds % 60);

        int width = goalMode ? 260 : (suddenDeath ? 220 : 120);
        int height = 36;
        int x = (panel.getWidth() - width) / 2;
        int y = 98;
        graphics.setColor(new Color(10, 10, 22, 220));
        graphics.fillRoundRect(x, y, width, height, 20, 20);
        graphics.setColor(new Color(255, 255, 255, 120));
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRoundRect(x, y, width, height, 20, 20);
        graphics.setStroke(new BasicStroke(1f));

        boolean urgent = !goalMode && (suddenDeath
            || (game.getEstado() == Partido.Estado.JUGANDO && seconds <= 10));
        graphics.setFont(new Font("Arial", Font.BOLD, (goalMode || suddenDeath) ? 20 : 24));
        graphics.setColor(urgent ? new Color(255, 90, 80) : Color.WHITE);
        drawCenteredText(graphics, text, x + width / 2, y + 26);
    }

    private void drawPowerBar(Graphics2D graphics, JPanel panel, Jugador player,
                              int x, int y, Color fillColor) {
        int width = 240;
        int height = 24;
        double charge = Math.max(0, Math.min(100, player.getCargaPoder()));
        boolean ready = charge >= 100;

        graphics.setColor(new Color(10, 10, 22, 220));
        graphics.fillRoundRect(x, y, width, height, 14, 14);
        graphics.setColor(ready ? SELECTED_COLOR : fillColor);
        graphics.fillRoundRect(x, y, (int) (width * charge / 100.0), height, 14, 14);
        graphics.setColor(new Color(255, 255, 255, 150));
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRoundRect(x, y, width, height, 14, 14);
        graphics.setStroke(new BasicStroke(1f));

        String text;
        if (player.tienePoder()) text = "PODER ACTIVO";
        else if (ready) text = "¡PODER LISTO!";
        else text = "PODER " + (int) charge + "%";
        graphics.setFont(new Font("Arial", Font.BOLD, 14));
        graphics.setColor(ready && !player.tienePoder() ? Color.BLACK : Color.WHITE);
        drawCenteredText(graphics, text, x + width / 2, y + 17);
    }
    
    private void drawLifeBar(Graphics2D graphics, JPanel panel, Jugador player, int x, int y, Color fillColor) {
		int width = 240;
		int height = 24;
		double charge = Math.max(0, Math.min(100, player.getVida()));
		boolean ready = charge >= 100;
		
		graphics.setColor(new Color(10, 10, 22, 220));
		graphics.fillRoundRect(x, y, width, height, 14, 14);
		graphics.setColor(ready ? SELECTED_COLOR : fillColor);
		graphics.fillRoundRect(x, y, (int) (width * charge / 100.0), height, 14, 14);
		graphics.setColor(new Color(255, 255, 255, 150));
		graphics.setStroke(new BasicStroke(2f));
		graphics.drawRoundRect(x, y, width, height, 14, 14);
		graphics.setStroke(new BasicStroke(1f));
		
		String text;
		if (ready) text = "VIDA AL 100";
		else text = (int) player.getVida()  + " de " + (int) player.getVidaMaxima();
		graphics.setFont(new Font("Arial", Font.BOLD, 14));
		graphics.setColor(ready && !player.tienePoder() ? Color.BLACK : Color.WHITE);
		drawCenteredText(graphics, text, x + width / 2, y + 17);
	}
    
    private void drawBloqueo(Graphics2D graphics,JPanel panel,Jugador player, int x , int y, Image blockedIcon) {
    	boolean bloqueo = player.estaAturdido();
    	if(bloqueo) {
    		
    		graphics.drawImage((BufferedImage) blockedIcon, null, x, y);
    	}
    	
    }

    private void drawCenteredText(Graphics2D graphics, String text, int centerX, int baselineY) {
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    
}
