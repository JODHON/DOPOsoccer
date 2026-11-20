package Presentation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JPanel;

import Domain.DOPOsoccer;
import Domain.Cancha;

public class GoalRenderer {
    public void draw(Graphics2D graphics, JPanel observer, DOPOsoccer game, Image goalImage) {
        drawGoal(graphics, observer, game.getCanchader(), goalImage, Color.BLUE, false);
        drawGoal(graphics, observer, game.getCanchaizq(), goalImage, Color.RED, true);
    }

    private void drawGoal(Graphics2D graphics, JPanel observer, Cancha goal,
                          Image image, Color fallback, boolean flipped) {
        int x = (int) goal.getX();
        int y = (int) goal.getY();
        int width = (int) goal.getAncho();
        int height = (int) goal.getAltura();
        if (image == null) {
            graphics.setColor(fallback);
            graphics.fillRect(x, y, width, height);
        } else if (flipped) {
            graphics.drawImage(image, x + width, y, x, y + height,
                0, 0, image.getWidth(observer), image.getHeight(observer), observer);
        } else {
            graphics.drawImage(image, x, y, width, height, observer);
        }
    }
}
