package Presentation;


import java.awt.Color;

import java.awt.Graphics2D;
import java.awt.Image;

import java.util.Map;

import javax.swing.JPanel;

import Domain.Jugador;


public class PlayerRenderer {
	public void draw(Graphics2D graphics, JPanel observer, Jugador player,
	 Map<String, Image> sprites, Image aura,
	boolean flipped, Color fallbackColor) {
		if (player.tienePoder() && aura != null) {
		   drawAura(graphics, observer, player, aura);
		}
		
		Image sprite = sprites.get(player.getEstado());
		if (sprite == null) sprite = sprites.get("idle");
		
		int x = (int) player.getX();
		int y = (int) player.getY();
		int right = x + player.getAncho();
		int bottom = y + player.getAlto();
		
		if (sprite == null) {
		   graphics.setColor(fallbackColor);
		   graphics.fillRect(x, y, player.getAncho(), player.getAlto());
		} else if (flipped) {
		   graphics.drawImage(sprite, right, y, x, bottom,
		       0, 0, sprite.getWidth(observer), sprite.getHeight(observer), observer);
		} else {
		   graphics.drawImage(sprite, x, y, player.getAncho(), player.getAlto(), observer);
		}
	}
	
	private void drawAura(Graphics2D graphics, JPanel observer, Jugador player, Image aura) {
		int frameCount = 4;
		int frameWidth = aura.getWidth(observer) / frameCount;
		int frameHeight = aura.getHeight(observer);
		int frame = (int) ((System.currentTimeMillis() / 100) % frameCount);
		int width = (int) (player.getAncho() * 1.4);
		int height = (int) (player.getAlto() * 1.2);
		int x = (int) player.getX() + (player.getAncho() - width) / 2;
		int y = (int) player.getY() + player.getAlto() - height;
		
		graphics.drawImage(aura, x, y, x + width, y + height,
		   frame * frameWidth, 0, (frame + 1) * frameWidth, frameHeight, observer);
	}
	
}

