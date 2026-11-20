package Presentation;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;
import Domain.Balon;
import Domain.DOPOsoccer;
import Presentation.GameRenderer.GameAssets;

public class FieldRenderer {




    public void draw(Graphics2D graphics, JPanel observer, DOPOsoccer game, GameAssets assets) {
        drawBackground(graphics, observer, assets.getBackground());
        drawBalls(graphics, observer, game, assets.getBall());
    }

    private void drawBackground(Graphics2D graphics, JPanel observer, Image background) {
        if (background != null) {
            graphics.drawImage(background, 0, 0, observer.getWidth(), observer.getHeight(), observer);
        } else {
            graphics.setColor(new Color(135, 206, 235));
            graphics.fillRect(0, 0, observer.getWidth(), 400);
            graphics.setColor(new Color(34, 139, 34));
            graphics.fillRect(0, 400, observer.getWidth(), observer.getHeight() - 400);
        }
    }

    private void drawBalls(Graphics2D graphics, JPanel observer, DOPOsoccer game, Image ballImage) {
        if (ballImage != null) {
            for (Balon fakeBall : game.getBalonesFalsos()) {
                drawBall(graphics, observer, fakeBall, ballImage);
            }
        }

        Balon ball = game.getBalon();
        if (ballImage != null) {
            drawBall(graphics, observer, ball, ballImage);
        } else {
            graphics.setColor(Color.WHITE);
            graphics.fillOval((int) ball.getX(), (int) ball.getY(), ball.getSize(), ball.getSize());
            graphics.setColor(Color.BLACK);
            graphics.drawOval((int) ball.getX(), (int) ball.getY(), ball.getSize(), ball.getSize());
        }
    }

    private void drawBall(Graphics2D graphics, JPanel observer, Balon ball, Image image) {
        graphics.drawImage(image, (int) ball.getX(), (int) ball.getY(),
            ball.getSize(), ball.getSize(), observer);
    }
}	

